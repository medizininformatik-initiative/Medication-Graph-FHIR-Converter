package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Matches German pharmaceutical products to RxNorm concepts using the propagation approach.
 * 
 * Strategy:
 * 1. Extract active ingredients with RxCUI codes from Substance level
 * 2. Normalize strengths using UCUM
 * 3. Map dose forms to RxNorm-compatible strings
 * 4. Generate SCD/SBD candidates via RxNav API
 * 5. Score and select best matches
 */
public final class RxNormProductMatcher {

    private static final String RXNAV_BASE_URL = "https://rxnav.nlm.nih.gov/REST";
    private static final double STRENGTH_TOLERANCE = 0.1; // 10% tolerance for strength matching
    private static final double MIN_MATCH_SCORE = 0.9; // Minimum score to accept a match (temporarily relaxed)

    // Ucum normalization via static utility methods

    // Optional resolver to determine RxNorm term type (e.g., IN, PIN) for a given RXCUI
    private static RxcuiTermTypeResolver rxcuiTermTypeResolver;
    private static RxNormCandidateProvider candidateProvider;

    // Small cache to reduce repeated lookups for the same RXCUI
    private final Map<String, String> rxcuiToTtyCache = new java.util.concurrent.ConcurrentHashMap<>();

    public RxNormProductMatcher() {}

    /**
     * Allows wiring a resolver which can tell us the RxNorm term type (TTY) for a given RXCUI.
     * If not provided, selection will fall back to a naive strategy.
     */
    public static void setRxcuiTermTypeResolver(RxcuiTermTypeResolver resolver) {
        rxcuiTermTypeResolver = resolver;
    }

    /**
     * Allows wiring a provider to fetch SCD/SBD candidates (from RxNav or a local dump/DB).
     */
    public static void setCandidateProvider(RxNormCandidateProvider provider) {
        candidateProvider = provider;
    }

    /**
     * Matches a drug to a Semantic Clinical Drug (SCD) concept.
     * SCD = ingredient(s) + strength(s) + dose form (generic)
     */
    @Nullable
    public MatchResult matchSCD(@NotNull GraphDrug drug) {
        System.out.println("\n=== [Matcher] Processing SCD for drug ===");
        System.out.println("[Matcher] Drug ingredients: " + drug.ingredients().stream()
                .map(gi -> gi.getSubstanceName() + " " + gi.getMassFrom() + "-" + gi.getMassTo() + " " + gi.getUnit())
                .collect(Collectors.joining(", ")));
        System.out.println("[Matcher] Drug dose form: " + drug.mmiDoseForm());
        
        if (drug.ingredients().isEmpty()) {
            System.out.println("[Matcher] REJECTED: No ingredients");
            return null;
        }

        // 1. fetch ingredient data
        List<IngredientMatch> ingredientMatches = prepareIngredientMatches(drug.ingredients());
        if (ingredientMatches.isEmpty()) {
            System.out.println("[Matcher] REJECTED: No valid ingredient matches");
            return null;
        }

        // 2. Get normalized dose form
        String rxdoseForm = getRxNormDoseForm(drug);
        System.out.println("[Matcher] RxNorm dose form: " + rxdoseForm);
        if (rxdoseForm == null) {
            return null;
        }

        // 3. Generate SCD candidates
        List<RxNormCandidate> candidates = generateSCDCandidates(ingredientMatches, rxdoseForm);
        if (candidates.isEmpty()) {
            return null;
        }

        // 4. Score and select best match
        return selectBestMatch(candidates, drug, MatchType.SCD);
    }

    /**
     * Matches a drug to a Semantic Branded Drug (SBD) concept.
     * SBD = SCD + brand/manufacturer name
     */
    @Nullable
    public MatchResult matchSBD(@NotNull GraphDrug drug) {
        System.out.println("\n=== [Matcher] Processing SBD for drug ===");
        
        // First try to get SCD as base
        MatchResult scdResult = matchSCD(drug);
        if (scdResult == null) {
            return null;
        }

        // Use provider if available to find SBDs related to best SCD
        if (candidateProvider != null) {
            List<RxNormCandidate> sbdCandidates = candidateProvider.findSbdCandidates(drug, scdResult);
            if (sbdCandidates != null && !sbdCandidates.isEmpty()) {
                MatchResult sbd = selectBestMatch(sbdCandidates, drug, MatchType.SBD);
                if (sbd != null) return sbd;
            }
        }

        // Fallback: return SCD result as SBD (neutral transformation)
        return new MatchResult(
                scdResult.rxcui,
                scdResult.name,
                MatchType.SBD,
                scdResult.score,
                scdResult.confidence,
                scdResult.matchingDetails
        );
    }

    /**
     * Prepares ingredient matches by selecting IN/PIN and normalizing strengths.
     */
    private List<IngredientMatch> prepareIngredientMatches(@NotNull List<GraphIngredient> ingredients) {
        return ingredients.stream()
            .filter(ingredient -> ingredient.isActive()) // Added: include just active ingredients! 
            .map(this::createIngredientMatch)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Creates an ingredient match by selecting the best RxCUI (IN vs PIN) and normalizing strength.
     */
    @Nullable
    private IngredientMatch createIngredientMatch(@NotNull GraphIngredient ingredient) {
        // 1. Select best RxCUI (prefer PIN over IN)
        String selectedRxcui = selectBestRxcui(ingredient.getRxcuiCodes());
        System.out.println("[Matcher] selectBestRxcui result: substanceName='" + ingredient.getSubstanceName() + "', rxcuiCodes=" + ingredient.getRxcuiCodes() + ", selectedRxcui=" + selectedRxcui);
        if (selectedRxcui == null && candidateProvider != null) {
            // Fallback: resolve ingredient RxCUI by substance name via RxNav
            System.out.println("[Matcher] Using RxNav fallback for substance: '" + ingredient.getSubstanceName() + "'");
            String resolved = candidateProvider.resolveIngredientRxcuiByName(ingredient.getSubstanceName());
            if (resolved != null && !resolved.isBlank()) {
                selectedRxcui = resolved;
                System.out.println("[Matcher] RxNav fallback successful: substanceName='" + ingredient.getSubstanceName() + "', resolvedRxcui=" + resolved);
            } else {
                System.out.println("[Matcher] RxNav fallback failed: substanceName='" + ingredient.getSubstanceName() + "', resolvedRxcui=null");
            }
        }
        if (selectedRxcui == null) {
            return null;
        }

        // 2. Normalize strength (guard against missing unit)
        GraphUnit unit = ingredient.getUnit();
        if (unit == null || unit.ucumCs() == null) {
            return null;
        }
        NormalizedStrength normalizedStrength = UcumNormalizer.normalize(
            ingredient.getMassFrom(),
            ingredient.getMassTo(),
            unit.ucumCs()
        );

        return new IngredientMatch(
            selectedRxcui,
            ingredient.getSubstanceName(),
            normalizedStrength,
            unit.ucumCs()
        );
    }

    /**
     * Selects the best RxCUI from available codes (prefer PIN over IN).
     */
    @Nullable
    private String selectBestRxcui(@NotNull List<String> rxcuiCodes) {
        if (rxcuiCodes.isEmpty()) {
            return null;
        }

        // Prefer PIN over IN if we can determine TTY via resolver
        if (rxcuiTermTypeResolver != null) {
            String pin = null;
            String in = null;
            for (String rxcui : rxcuiCodes) {
                String tty = rxcuiToTtyCache.computeIfAbsent(rxcui, key -> safeResolveTty(key));
                if (tty == null) continue;
                String ttyUpper = tty.toUpperCase(Locale.ROOT);
                if (pin == null && ttyUpper.equals("PIN")) {
                    pin = rxcui;
                    // We can short-circuit if we already have a PIN
                    if (pin != null) {
                        return pin;
                    }
                } else if (in == null && ttyUpper.equals("IN")) {
                    in = rxcui;
                }
            }
            if (pin != null) return pin;
            if (in != null) return in;
        }

        // Fallback: return first available RXCUI when TTY information is unavailable
        return rxcuiCodes.get(0);
    }

    /**
     * Gets the RxNorm-compatible dose form for the drug.
     */
    @Nullable
    private String getRxNormDoseForm(@NotNull GraphDrug drug) {
        // Debug: Show MMI and EDQM dose forms
        String mmiDoseForm = drug.mmiDoseForm();
        GraphEdqmPharmaceuticalDoseForm edqmDoseForm = drug.edqmDoseForm();
        
        System.out.println("[Matcher] Dose form analysis:");
        System.out.println("[Matcher]   MMI dose form: " + (mmiDoseForm != null ? mmiDoseForm : "null"));
        System.out.println("[Matcher]   EDQM dose form: " + (edqmDoseForm != null ? 
            "code=" + edqmDoseForm.getCode() + ", name=" + edqmDoseForm.getName() : "null"));
        
        // Check if MMI → EDQM mapping exists in Neo4j
        if (mmiDoseForm != null && edqmDoseForm == null) {
            System.out.println("[Matcher]   WARNING: MMI dose form exists but no EDQM mapping found!");
            System.out.println("[Matcher]   This means the MMI dose form '" + mmiDoseForm + "' is not mapped to any EDQM dose form in the database.");
        }
        
        // Try EDQM → RxNorm via DB mapping
        String edqmMapping = DoseFormMapper.mapEdqm(edqmDoseForm);
        System.out.println("[Matcher]   EDQM mapping result: " + (edqmMapping != null ? edqmMapping : "null"));
        
        return edqmMapping;
    }

    /**
     * Generates SCD candidates using RxNav API.
     */
    private List<RxNormCandidate> generateSCDCandidates(
            @NotNull List<IngredientMatch> ingredients,
            @NotNull String doseForm) {
        if (candidateProvider == null) return java.util.Collections.emptyList();
        List<RxNormCandidate> candidates = candidateProvider.findScdCandidates(ingredients, doseForm);
        return candidates == null ? java.util.Collections.emptyList() : candidates;
    }

    /**
     * Scores candidates and selects the best match.
     */
    @Nullable
    private MatchResult selectBestMatch(
            @NotNull List<RxNormCandidate> candidates,
            @NotNull GraphDrug drug,
            @NotNull MatchType matchType) {
        if (candidates.isEmpty()) {
            return null;
        }

        // Validate and score
        double bestScore = -1.0;
        RxNormCandidate best = null;
        Map<String, Object> bestDetails = null;

        for (RxNormCandidate c : candidates) {
            Map<String, Object> details = new HashMap<>();
            boolean valid = validateCandidate(c, drug, details);
            if (!valid) continue;
            double score = computeScore(c, drug, details);
            if (score > bestScore) {
                bestScore = score;
                best = c;
                bestDetails = details;
            }
        }

        if (best == null || bestScore < MIN_MATCH_SCORE) {
            //Debugging
            System.out.println("[Matcher] no acceptable match (bestScore=" + bestScore + 
                    ", min=" + MIN_MATCH_SCORE + ") candidates=" + candidates.size());
            return null;
        }

        double confidence = Math.min(1.0, Math.max(0.0, bestScore));
        MatchResult result = new MatchResult(best.rxcui, best.name, matchType, bestScore, confidence, bestDetails);
        System.out.println("[Matcher] SELECTED match rxcui=" + result.rxcui +
                ", name='" + result.name + "', type=" + result.type +
                ", score=" + result.score + ", confidence=" + result.confidence +
                ", details=" + result.matchingDetails);
        return result;
    }

    private boolean validateCandidate(RxNormCandidate candidate, GraphDrug drug, Map<String, Object> details) {
        // Dose form must match exactly (case-insensitive)
        String expectedForm = getRxNormDoseForm(drug);
        if (expectedForm == null) return false;
        boolean formOk = expectedForm.equalsIgnoreCase(candidate.doseForm);
        details.put("doseFormMatch", formOk);
        //Debugging
        System.out.println("[Matcher] doseForm expected='" + expectedForm + "' Rx-SCD-candidate='" + candidate.doseForm + "' => " + formOk);
        if (!formOk) return false;

        // Ingredients must be compatible with bidirectional comparison
        // Both sets must contain exactly the same ingredients (allowing PIN/IN flexibility)
        java.util.Set<String> expectedIngs = drug.ingredients().stream()
                .filter(ingredient -> ingredient.isActive()) // Only active ingredients
                .map(this::createIngredientMatch)
                .filter(Objects::nonNull)
                .map(im -> im.rxcui)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Set<String> candidateIngs = new java.util.HashSet<>(candidate.ingredients);
        boolean ingredientsOk = candidateIngs.isEmpty() || areIngredientsCompatible(expectedIngs, candidateIngs);
        details.put("ingredientsMatch", ingredientsOk);
        //Debugging
        System.out.println("[Matcher] Bidirectional ingredient comparison:");
        System.out.println("[Matcher]   MMI active ingredients (expected): " + expectedIngs);
        System.out.println("[Matcher]   RxNorm SCD ingredients (candidate): " + candidateIngs);
        System.out.println("[Matcher]   Result: " + ingredientsOk);
        if (!ingredientsOk) return false;

        // Strengths: strict validation - all strengths must match within tolerance
        boolean strengthOk = true;
        boolean comparedAny = false;
        for (GraphIngredient gi : drug.ingredients()) {
            IngredientMatch im = createIngredientMatch(gi);
            if (im == null) continue;
            BigDecimal expected = im.normalizedStrength.amountFrom();
            BigDecimal candidateStrength = candidate.strengths != null ? candidate.strengths.get(im.rxcui) : null;
            if (expected == null) continue; // skip ingredients without expected strength
            comparedAny = true; // Set comparedAny as soon as we have a valid expected strength
            if (candidateStrength == null || !withinTolerance(expected, candidateStrength, STRENGTH_TOLERANCE)) {
                strengthOk = false;
                break;
            }
        }
        // If we had nothing to compare, don't fail on strengths
        if (!comparedAny) strengthOk = true;
        details.put("strengthMatch", strengthOk);
        //Debugging
        System.out.println("[Matcher] strengths comparedAny=" + comparedAny + " result=" + strengthOk);
        return strengthOk;
    }

    /**
     * Checks if ingredient sets are compatible with bidirectional comparison.
     * Both sets must contain exactly the same ingredients (allowing PIN/IN flexibility).
     * PIN and IN for the same substance are considered compatible.
     */
    private boolean areIngredientsCompatible(java.util.Set<String> expected, java.util.Set<String> candidate) {
        // If candidate is empty, it's not compatible! 
        if (candidate.isEmpty()) return false;
        
        // Bidirectional comparison: both sets must be identical
        // 1. Check if all expected ingredients have compatible matches in candidate
        for (String expectedRxcui : expected) {
            boolean hasCompatibleMatch = false;
            
            // Direct match
            if (candidate.contains(expectedRxcui)) {
                hasCompatibleMatch = true;
            } else {
                // Check for PIN/IN compatibility
                for (String candidateRxcui : candidate) {
                    if (areRxcuisCompatible(expectedRxcui, candidateRxcui)) {
                        hasCompatibleMatch = true;
                        break;
                    }
                }
            }
            
            if (!hasCompatibleMatch) {
                System.out.println("[Matcher] Bidirectional check failed (neo4j->rxcui): expected ingredient " + expectedRxcui + " not found in candidate");
                return false;
            }
        }
        
        // 2. Check if all candidate ingredients have compatible matches in expected (reverse check)
        for (String candidateRxcui: candidate) {
            boolean hasCompatibleMatch = false; 

            if (expected.contains(candidateRxcui)) {
                hasCompatibleMatch = true;  
            }
            //Check for PIN/IN compatibility
            else {
                for (String expectedRxcui: expected) {
                    if (areRxcuisCompatible(expectedRxcui, candidateRxcui)) {
                        hasCompatibleMatch = true; 
                        break; 
                    }
                }
            }
            
        if (!hasCompatibleMatch) {
            System.out.println("[Matcher] Bidirectional check failed (reverse check): candidate ingredient " + candidateRxcui + " not found in expected");
            return false;
        }
        }
        
        System.out.println("[Matcher] Bidirectional check passed: ingredient sets are identical");
        return true;
    }
    
    /**
     * Checks if two RxCUIs are compatible (PIN/IN for same substance).
     * Query RxNav to get the actual substance relationships.
     */
    private boolean areRxcuisCompatible(String rxcui1, String rxcui2) {
        // If they're the same, they're compatible
        if (rxcui1.equals(rxcui2)) {
            return true;
        }
        
        try {
            // Query RxNav for related concepts (PIN/IN) for rxcui1
            String url = RXNAV_BASE_URL + "/rxcui/" + rxcui1 + "/related?tty=PIN+IN&format=json";
            JsonNode response = makeApiCall(url);
            
            if (response != null && response.has("relatedGroup")) {
                JsonNode relatedGroup = response.get("relatedGroup");
                if (relatedGroup.has("conceptGroup")) {
                    JsonNode conceptGroup = relatedGroup.get("conceptGroup");
                    
                    // Check if rxcui2 is in the related concepts
                    for (JsonNode group : conceptGroup) {
                        if (group.has("conceptProperties")) {
                            JsonNode conceptProperties = group.get("conceptProperties");
                            for (JsonNode concept : conceptProperties) {
                                if (concept.has("rxcui") && concept.get("rxcui").asText().equals(rxcui2)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            
            // Also check the reverse: query rxcui2 for related concepts
            String reverseUrl = RXNAV_BASE_URL + "/rxcui/" + rxcui2 + "/related?tty=PIN+IN&format=json";
            JsonNode reverseResponse = makeApiCall(reverseUrl);
            
            if (reverseResponse != null && reverseResponse.has("relatedGroup")) {
                JsonNode relatedGroup = reverseResponse.get("relatedGroup");
                if (relatedGroup.has("conceptGroup")) {
                    JsonNode conceptGroup = relatedGroup.get("conceptGroup");
                    
                    // Check if rxcui1 is in the related concepts
                    for (JsonNode group : conceptGroup) {
                        if (group.has("conceptProperties")) {
                            JsonNode conceptProperties = group.get("conceptProperties");
                            for (JsonNode concept : conceptProperties) {
                                if (concept.has("rxcui") && concept.get("rxcui").asText().equals(rxcui1)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            // If API call fails, fall back to false
            System.err.println("[Matcher] Error checking RxCUI compatibility: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Makes an API call to RxNav and returns the JSON response.
     */
    private JsonNode makeApiCall(String url) {
        try {
            // Use the existing RxNavCandidateProvider's HTTP client
            if (candidateProvider instanceof RxNavCandidateProvider) {
                return ((RxNavCandidateProvider) candidateProvider).makeApiCall(url);
            } else {
                System.err.println("[Matcher] No RxNavCandidateProvider available for API calls");
                return null;
            }
        } catch (Exception e) {
            System.err.println("[Matcher] API call failed for URL: " + url + ", error: " + e.getMessage());
            return null;
        }
    }

    private boolean withinTolerance(BigDecimal expected, BigDecimal actual, double relTolerance) {
        if (expected.signum() == 0) return actual.signum() == 0; // both zero
        java.math.BigDecimal diff = actual.subtract(expected).abs();
        java.math.BigDecimal rel = diff.divide(expected.abs(), java.math.MathContext.DECIMAL64);
        return rel.doubleValue() <= relTolerance;
    }

    private double computeScore(RxNormCandidate candidate, GraphDrug drug, Map<String, Object> details) {
        double score = 0.0;
        // Binary features
        if (Boolean.TRUE.equals(details.get("doseFormMatch"))) score += 0.3;
        if (Boolean.TRUE.equals(details.get("ingredientsMatch"))) score += 0.3;
        if (Boolean.TRUE.equals(details.get("strengthMatch"))) score += 0.3;

        // Minor bonus: exact case-sensitive form name
        String expectedForm = getRxNormDoseForm(drug);
        if (expectedForm != null && expectedForm.equals(candidate.doseForm)) score += 0.1;

        return score;
    }

    // Inner classes for data structures

    public enum MatchType {
        SCD, SBD
    }

    public static class MatchResult {
        public final String rxcui;
        public final String name;
        public final MatchType type;
        public final double score;
        public final double confidence;
        public final Map<String, Object> matchingDetails;

        public MatchResult(String rxcui, String name, MatchType type, double score, 
                          double confidence, Map<String, Object> matchingDetails) {
            this.rxcui = rxcui;
            this.name = name;
            this.type = type;
            this.score = score; // Match-Qualität (0.0-1.0)
            this.confidence = confidence;
            this.matchingDetails = matchingDetails;
        }
    }

    public static class IngredientMatch {
        public final String rxcui;
        public final String substanceName;
        public final NormalizedStrength normalizedStrength; // UCUM-normalisierte Stärke
        public final String originalUnit;

        public IngredientMatch(String rxcui, String substanceName, 
                              NormalizedStrength normalizedStrength, String originalUnit) {
            this.rxcui = rxcui;
            this.substanceName = substanceName;
            this.normalizedStrength = normalizedStrength;
            this.originalUnit = originalUnit;
        }
    }

    public static class RxNormCandidate {
        public final String rxcui;
        public final String name;
        public final String tty; // Term Type (SCD, SBD, IN etc.)
        public final List<String> ingredients;
        public final Map<String, BigDecimal> strengths;
        public final String doseForm;

        public RxNormCandidate(String rxcui, String name, String tty, 
                              List<String> ingredients, Map<String, BigDecimal> strengths, String doseForm) {
            this.rxcui = rxcui;
            this.name = name;
            this.tty = tty;
            this.ingredients = ingredients;
            this.strengths = strengths;
            this.doseForm = doseForm;
        }
    }

    /**
     * Resolves the TTY using the configured resolver, swallowing any exception to keep matching robust.
     */
    private String safeResolveTty(String rxcui) {
        try {
            return rxcuiTermTypeResolver != null ? rxcuiTermTypeResolver.resolveTty(rxcui) : null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    /**
     * SPI used to obtain the RxNorm Term Type (TTY) for a given RXCUI (e.g., IN, PIN).
     * Implementations may call RxNav or query a local dump/DB. Should return null if unknown.
     */
    public interface RxcuiTermTypeResolver {
        @Nullable String resolveTty(@NotNull String rxcui);
    }

    /**
     * SPI to supply RxNorm candidates from an external source (RxNav or local dump/DB).
     */
    public interface RxNormCandidateProvider {
        @NotNull List<RxNormCandidate> findScdCandidates(@NotNull List<IngredientMatch> ingredients, @NotNull String doseForm);

        @NotNull List<RxNormCandidate> findSbdCandidates(@NotNull GraphDrug drug, @NotNull MatchResult scdBaseMatch);

        /**
         * Optional helper to resolve an ingredient RXCUI by English substance name via RxNav.
         * Implementations may return null if not supported.
         */
        @org.jetbrains.annotations.Nullable String resolveIngredientRxcuiByName(String substanceName);
    }
}
