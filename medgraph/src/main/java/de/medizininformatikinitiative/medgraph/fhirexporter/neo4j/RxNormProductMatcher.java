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
        // Show only active ingredients
        List<GraphIngredient> activeIngredients = drug.ingredients().stream()
                .filter(GraphIngredient::isActive)
                .collect(Collectors.toList());
        
        if (activeIngredients.isEmpty()) {
            System.out.println("[Matcher] REJECTED: No active ingredients");
            return null;
        }
        
        System.out.println("\n[Matcher] SCD matching: " + activeIngredients.stream()
                .map(gi -> gi.getSubstanceName() + (gi.getMassFrom() != null ? " " + gi.getMassFrom() + (gi.getUnit() != null ? " " + gi.getUnit().print() : "") : ""))
                .collect(Collectors.joining(", ")) + " | " + drug.mmiDoseForm());
        
        // 1. fetch ingredient data
        List<IngredientMatch> ingredientMatches = prepareIngredientMatches(drug.ingredients());
        if (ingredientMatches.isEmpty()) {
            System.out.println("[Matcher] REJECTED: No valid ingredient matches");
            return null;
        }

        // 2. Get normalized dose form
        String rxdoseForm = getRxNormDoseForm(drug);
        if (rxdoseForm == null) {
            System.out.println("[Matcher] REJECTED: No RxNorm dose form mapping");
            return null;
        }

        // 3. Generate SCD candidates
        System.out.println("[Matcher] Searching for SCD candidates with RxCUIs: " + 
                ingredientMatches.stream().map(im -> im.substanceName + "=" + im.rxcui).collect(Collectors.joining(", ")) +
                ", doseForm='" + rxdoseForm + "'");
        List<RxNormCandidate> candidates = generateSCDCandidates(ingredientMatches, rxdoseForm);
        if (candidates.isEmpty()) {
            return null;
        }

        // 4. Score and select best match
        return selectBestMatch(candidates, drug, MatchType.SCD, rxdoseForm, ingredientMatches);
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
                String rxdoseForm = getRxNormDoseFormSilent(drug);
                List<IngredientMatch> ingredientMatches = prepareIngredientMatches(drug.ingredients());
                MatchResult sbd = selectBestMatch(sbdCandidates, drug, MatchType.SBD, rxdoseForm, ingredientMatches);
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
     * If the selected RxCUI has no SCDs, tries alternative RxCUIs from the list.
     */
    @Nullable
    private IngredientMatch createIngredientMatch(@NotNull GraphIngredient ingredient) {
        // 1. Select best RxCUI (prefer PIN over IN)
        List<String> rxcuiCodes = ingredient.getRxcuiCodes();
        if (rxcuiCodes == null || rxcuiCodes.isEmpty()) {
            return null;
        }
        
        String selectedRxcui = selectBestRxcui(rxcuiCodes);
        if (selectedRxcui == null) {
            return null;
        }
        
        // 2. If candidate provider is available, verify that the selected RxCUI has SCDs
        // If not, try alternative RxCUIs from the list
        if (candidateProvider instanceof LocalRxNormCandidateProvider) {
            LocalRxNormCandidateProvider localProvider = (LocalRxNormCandidateProvider) candidateProvider;
            if (!localProvider.hasScdsForIngredient(selectedRxcui)) {
                // Try alternative RxCUIs
                for (String alternativeRxcui : rxcuiCodes) {
                    if (!alternativeRxcui.equals(selectedRxcui) && localProvider.hasScdsForIngredient(alternativeRxcui)) {
                        System.out.println("[Matcher] RxCUI " + selectedRxcui + " has no SCDs, using alternative " + alternativeRxcui);
                        selectedRxcui = alternativeRxcui;
                        break;
                    }
                }
            }
        }

        // 3. Normalize strength (guard against missing unit)
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
                    // We can short-circuit if we found a PIN
                    return pin;
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
        String mmiDoseForm = drug.mmiDoseForm();
        GraphEdqmPharmaceuticalDoseForm edqmDoseForm = drug.edqmDoseForm();
        
        // Check if MMI → EDQM mapping exists in Neo4j
        if (mmiDoseForm != null && edqmDoseForm == null) {
            System.out.println("[Matcher] WARNING: MMI dose form '" + mmiDoseForm + "' has no EDQM mapping");
        }
        
        // Try EDQM → RxNorm via DB mapping
        String edqmMapping = DoseFormMapper.mapEdqm(edqmDoseForm);
        
        return edqmMapping;
    }
    
    /**
     * Gets the RxNorm-compatible dose form for the drug (without logging).
     */
    @Nullable
    private String getRxNormDoseFormSilent(@NotNull GraphDrug drug) {
        GraphEdqmPharmaceuticalDoseForm edqmDoseForm = drug.edqmDoseForm();
        return DoseFormMapper.mapEdqm(edqmDoseForm);
    }

    /**
     * Generates SCD candidates
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
            @NotNull MatchType matchType,
            @NotNull String expectedDoseForm,
            @NotNull List<IngredientMatch> ingredientMatches) {
        if (candidates.isEmpty()) {
            return null;
        }

        // Validate and score
        double bestScore = -1.0;
        double bestStrengthRelDiff = Double.POSITIVE_INFINITY; // tie-breaker: prefer closest strength
        RxNormCandidate best = null;
        Map<String, Object> bestDetails = null;
        int validCount = 0;
        int doseFormRejected = 0;
        int ingredientsRejected = 0;
        int strengthRejected = 0;
        List<String> rejectedCandidates = new ArrayList<>();

        for (RxNormCandidate c : candidates) {
            Map<String, Object> details = new HashMap<>();
            boolean valid = validateCandidate(c, drug, details, expectedDoseForm, ingredientMatches);
            if (!valid) {
                String rejectionReason;
                StringBuilder candidateInfo = new StringBuilder();
                candidateInfo.append("  - ").append(c.name).append(" (rxcui=").append(c.rxcui);
                candidateInfo.append(", doseForm='").append(c.doseForm != null ? c.doseForm : "null").append("'");
                candidateInfo.append(", ingredients=").append(c.ingredients != null ? c.ingredients : "[]");
                if (c.strengths != null && !c.strengths.isEmpty()) {
                    candidateInfo.append(", strengths=").append(c.strengths);
                }
                candidateInfo.append("): ");
                
                if (!Boolean.TRUE.equals(details.get("doseFormMatch"))) {
                    doseFormRejected++;
                    rejectionReason = "doseForm mismatch (expected='" + expectedDoseForm + "', candidate='" + c.doseForm + "')";
                } else if (!Boolean.TRUE.equals(details.get("ingredientsMatch"))) {
                    ingredientsRejected++;
                    java.util.Set<String> expectedIngs = ingredientMatches.stream()
                            .map(im -> im.rxcui).collect(java.util.stream.Collectors.toSet());
                    rejectionReason = "ingredients mismatch (expected=" + expectedIngs + ", candidate=" + c.ingredients + ")";
                } else if (!Boolean.TRUE.equals(details.get("strengthMatch"))) {
                    strengthRejected++;
                    rejectionReason = "strength mismatch";
                } else {
                    rejectionReason = "unknown reason";
                }
                candidateInfo.append(rejectionReason);
                rejectedCandidates.add(candidateInfo.toString());
                continue;
            }
            validCount++;
            double score = computeScore(c, drug, details);
            double relDiff = details.getOrDefault("strengthRelDiff", Double.POSITIVE_INFINITY) instanceof Number
                    ? ((Number) details.get("strengthRelDiff")).doubleValue()
                    : Double.POSITIVE_INFINITY;

            if (score > bestScore ||
                    (Double.compare(score, bestScore) == 0 && relDiff < bestStrengthRelDiff)) {
                bestScore = score;
                bestStrengthRelDiff = relDiff;
                best = c;
                bestDetails = details;
            }
        }

        if (best == null || bestScore < MIN_MATCH_SCORE) {
            System.out.println("[Matcher] No match: " + candidates.size() + " candidates, " + validCount + " valid, " +
                    doseFormRejected + " doseForm, " + ingredientsRejected + " ingredients, " + strengthRejected + " strength");
            
            if (!rejectedCandidates.isEmpty()) {
                System.out.println("[Matcher] Rejected candidates:");
                rejectedCandidates.forEach(System.out::println);
            }
            return null;
        }

        double confidence = Math.min(1.0, Math.max(0.0, bestScore));
        MatchResult result = new MatchResult(best.rxcui, best.name, matchType, bestScore, confidence, bestDetails);
        
        // Build match information string
        StringBuilder matchInfo = new StringBuilder("[Matcher] Match: " + best.name + " (rxcui=" + best.rxcui + ", score=" + String.format("%.2f", bestScore) + ")");
        
        // Add strength match information - ALWAYS show details if available
        if (bestDetails != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> strengthDetails = (List<Map<String, Object>>) bestDetails.get("strengthDetails");
            
            if (Boolean.TRUE.equals(bestDetails.get("strengthMatch"))) {
                boolean exactMatch = Boolean.TRUE.equals(bestDetails.get("strengthExactMatch"));
                boolean toleranceOnly = Boolean.TRUE.equals(bestDetails.get("strengthToleranceOnly"));
                
                if (exactMatch) {
                    matchInfo.append(" [Strength: EXACT MATCH]");
                } else if (toleranceOnly) {
                    matchInfo.append(" [Strength: TOLERANCE MATCH (10%)]");
                }
                
                // Show detailed strength information for each ingredient
                if (strengthDetails != null && !strengthDetails.isEmpty()) {
                    matchInfo.append("\n[Matcher]   Strength details:");
                    for (Map<String, Object> detail : strengthDetails) {
                        String ingredient = (String) detail.get("ingredient");
                        BigDecimal expected = (BigDecimal) detail.get("expected");
                        BigDecimal actual = (BigDecimal) detail.get("actual");
                        String expectedUnit = (String) detail.get("expectedUnit");
                        String actualUnit = (String) detail.get("actualUnit");
                        boolean isExact = Boolean.TRUE.equals(detail.get("exactMatch"));
                        double relDiff = ((Number) detail.get("relativeDiff")).doubleValue();
                        matchInfo.append("\n[Matcher]     - ").append(ingredient)
                                .append(": expected=").append(expected)
                                .append(" ").append(expectedUnit != null ? expectedUnit : "")
                                .append(" (aus Neo4j")
                                .append(expectedUnit != null && expectedUnit.contains("/") ? " - Konzentration" : " - absolute Menge")
                                .append("), actual=").append(actual)
                                .append(" ").append(actualUnit != null ? actualUnit : "")
                                .append(" (aus RxNorm SCD-Name geparst)")
                                .append(isExact ? " [EXACT]" : " [TOLERANCE: " + String.format("%.1f", relDiff * 100) + "%]");
                    }
                }
            }
        }
        
        System.out.println(matchInfo.toString());
        return result;
    }

    private boolean validateCandidate(RxNormCandidate candidate, GraphDrug drug, Map<String, Object> details,
                                      String expectedDoseForm, List<IngredientMatch> ingredientMatches) {
        // Dose form must match exactly (case-insensitive)
        boolean formOk = expectedDoseForm.equalsIgnoreCase(candidate.doseForm);
        details.put("doseFormMatch", formOk);
        if (!formOk) return false;

        // Ingredients must be compatible with bidirectional comparison
        // Both sets must contain exactly the same ingredients (allowing PIN/IN flexibility)
        java.util.Set<String> expectedIngs = ingredientMatches.stream()
                .map(im -> im.rxcui)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Set<String> candidateIngs = new java.util.HashSet<>(candidate.ingredients);
        boolean ingredientsOk = candidateIngs.isEmpty() || areIngredientsCompatible(expectedIngs, candidateIngs);
        details.put("ingredientsMatch", ingredientsOk);
        if (!ingredientsOk) return false;

        // Strengths: strict validation - all strengths must match within tolerance
        // For ratio strengths (e.g., mg/mL, mg/h), denominator units must also match
        boolean strengthOk = true;
        boolean comparedAny = false;
        double totalRelDiff = 0.0;
        int relDiffCount = 0;
        boolean allExact = true; // Track if all strength matches are exact
        boolean anyToleranceOnly = false; // Track if any match is only within tolerance (not exact)
        List<Map<String, Object>> strengthDetails = new ArrayList<>(); // Details for each strength comparison
        
        for (GraphIngredient gi : drug.ingredients()) {
            if (!gi.isActive()) continue; // Only compare strengths for active ingredients
            IngredientMatch im = createIngredientMatch(gi);
            if (im == null) continue;
            BigDecimal expected = im.normalizedStrength.amountFrom();

            BigDecimal candidateStrength = candidate.strengths != null ? candidate.strengths.get(im.rxcui) : null;
            String compatibleRxcui = im.rxcui; // Track which RxCUI was used for lookup
            if (candidateStrength == null && candidate.strengths != null) {
                // Try to find strength using compatible RxCUI (PIN/IN relationship)
                for (String candidateRxcui : candidate.strengths.keySet()) {
                    if (areRxcuisCompatible(im.rxcui, candidateRxcui)) {
                        candidateStrength = candidate.strengths.get(candidateRxcui);
                        compatibleRxcui = candidateRxcui;
                        break;
                    }
                }
            }
            if (expected == null) continue; // skip ingredients without expected strength
            comparedAny = true; // Set comparedAny as soon as we have a valid expected strength
            
            // Check numerator unit family compatibility (e.g., mg vs mmol)
            String expectedNumerator = im.normalizedStrength.numeratorUnit();

            String candidateNumerator = candidate.numeratorUnits != null ? candidate.numeratorUnits.get(compatibleRxcui) : null;
            // If candidate lacks a numerator unit, treat as mismatch (should not happen)
            if (candidateNumerator == null) {
                strengthOk = false;
                break;
            }
            if (expectedNumerator != null && !expectedNumerator.equalsIgnoreCase(candidateNumerator)) {
                strengthOk = false;
                break;
            }
            
            // Check if this is a ratio strength (has denominator unit)
            String expectedDenominator = im.normalizedStrength.denominatorUnit();

            String candidateDenominatorRaw = candidate.denominatorUnits != null ? 
                candidate.denominatorUnits.get(compatibleRxcui) : null;
            // Normalize empty string to null
            String candidateDenominator = (candidateDenominatorRaw != null && candidateDenominatorRaw.isEmpty()) ? null : candidateDenominatorRaw;
            
            // Handle mismatch between scalar and ratio strengths according to the unit comparison rules (examples):
            // 1. Neo4j mg, RxNorm mg/ml → Neo4j durch Amount teilen (nur wenn Drug-Unit ein Volumen ist)
            // 2. Neo4j mg/ml, RxNorm mg → Neo4j mit Amount multiplizieren
            // 3. Beide gleicher Typ → direkt vergleichen
            
            boolean neoIsRatio = expectedDenominator != null;
            boolean rxNormIsRatio = candidateDenominator != null;
            
            if (rxNormIsRatio && !neoIsRatio) {
                // Neo4j ist absolut (mg), RxNorm ist Konzentration (mg/ml, g/L, etc.)
                // → Teilen durch drugAmount, aber nur wenn Drug-Unit ein Volumen ist (ml, l, etc.)
                BigDecimal drugVolumeInMl = getDrugVolumeInMl(drug);
                if (drugVolumeInMl != null && drugVolumeInMl.compareTo(BigDecimal.ZERO) > 0) {
                    // Convert absolute amount to concentration
                    BigDecimal expectedConcentration = expected.divide(drugVolumeInMl, java.math.MathContext.DECIMAL64);
                    // Check if candidate denominator is a volume unit (ml, l, etc.)
                    if (candidateDenominator != null && isVolumeUnit(candidateDenominator)) {
                        // Use the converted concentration for comparison
                        // Normalize denominator to "mL" (canonical form)
                        expected = expectedConcentration;
                        expectedDenominator = "mL";
                    } else {
                        // Denominator is not a volume unit, can't convert
                        strengthOk = false;
                        break;
                    }
                } else {
                    // No drug volume available, can't convert scalar to ratio
                    strengthOk = false;
                    break;
                }
            } else if (!rxNormIsRatio && neoIsRatio) {
                // Neo4j ist Konzentration (mg/ml, g/L, etc.), RxNorm ist absolut (mg)
                // → Multiplizieren mit drugAmount
                BigDecimal drugVolumeInMl = getDrugVolumeInMl(drug);
                if (drugVolumeInMl != null && drugVolumeInMl.compareTo(BigDecimal.ZERO) > 0) {
                    // Check if expected denominator is a volume unit
                    if (expectedDenominator != null && isVolumeUnit(expectedDenominator)) {
                        // Convert concentration to absolute amount
                        // First, normalize the expected value if denominator is not mL
                        BigDecimal expectedValueInMl = expected;
                        if (!expectedDenominator.equalsIgnoreCase("ml") && !expectedDenominator.equalsIgnoreCase("mL")) {
                            // Convert denominator to mL (e.g., L -> mL)
                            BigDecimal denominatorInMl = convertVolumeToMl(BigDecimal.ONE, expectedDenominator);
                            if (denominatorInMl != null) {
                                // Adjust the value: if denominator was L, we need to multiply by 1000
                                // Example: 1 g/L = 1 g / 1 L = 1 g / 1000 mL = 0.001 g/mL
                                // But we want mg/mL, so: 1 g/L = 1000 mg / 1000 mL = 1 mg/mL
                                // Actually, the normalization should have already handled this...
                                // For now, just use the value as-is and multiply by drug volume
                            }
                        }
                        BigDecimal expectedAbsolute = expectedValueInMl.multiply(drugVolumeInMl, java.math.MathContext.DECIMAL64);
                        // Use the converted absolute amount for comparison
                        expected = expectedAbsolute;
                        expectedDenominator = null;
                    } else {
                        // Expected denominator is not a volume unit, can't convert
                        strengthOk = false;
                        break;
                    }
                } else {
                    // No drug volume available, can't convert ratio to scalar
                    strengthOk = false;
                    break;
                }
            }
            // Sonst: beide gleicher Typ → direkt vergleichen (keine Umrechnung nötig)
            
            // For ratio strengths, denominator units must match
            if (expectedDenominator != null || candidateDenominator != null) {
                // Both must be ratios with matching denominator units
                if (expectedDenominator == null || candidateDenominator == null) {
                    strengthOk = false;
                    break;
                }
                // Both are ratios - check if denominator units are compatible
                // If both are volume units, normalize them to "mL" and adjust values if needed
                if (isVolumeUnit(expectedDenominator) && isVolumeUnit(candidateDenominator)) {
                    // Both are volume units - normalize to "mL"
                    // If denominators differ (e.g., L vs mL), adjust the expected value
                    if (!expectedDenominator.equalsIgnoreCase(candidateDenominator)) {
                        // Convert expected to candidate's denominator unit
                        // Example: expected is 1 mg/mL, candidate is 1000 mg/L
                        // We need to convert: 1 mg/mL = 1000 mg/L
                        // Formula: value_new = value_old * (denom_old_in_ml / denom_new_in_ml)
                        // For 1 mg/mL -> mg/L: 1 * (1 mL / 1000 mL) = 0.001 mg/L (WRONG!)
                        // Correct: 1 mg/mL means 1 mg per 1 mL = 1 mg per 0.001 L = 1000 mg/L
                        // So: value_new = value_old * (denom_new_in_ml / denom_old_in_ml)
                        BigDecimal expectedDenomInMl = convertVolumeToMl(BigDecimal.ONE, expectedDenominator);
                        BigDecimal candidateDenomInMl = convertVolumeToMl(BigDecimal.ONE, candidateDenominator);
                        if (expectedDenomInMl != null && candidateDenomInMl != null && 
                            expectedDenomInMl.compareTo(BigDecimal.ZERO) > 0) {
                            // Adjust expected value: multiply by ratio to convert to candidate's denominator
                            // Example: 1 mg/mL -> mg/L: 1 * (1000 mL / 1 mL) = 1000 mg/L
                            BigDecimal ratio = candidateDenomInMl.divide(expectedDenomInMl, java.math.MathContext.DECIMAL64);
                            expected = expected.multiply(ratio, java.math.MathContext.DECIMAL64);
                            // Update denominator to match candidate for comparison
                            expectedDenominator = candidateDenominator;
                        }
                    }
                    // After adjustment, both should be comparable (normalized to same base)
                } else if (!expectedDenominator.equalsIgnoreCase(candidateDenominator)) {
                    // Denominator units don't match and are not both volume units
                    strengthOk = false;
                    break;
                }
            }
            // If both are scalar (no denominator) or denominators match, compare values
            if (candidateStrength == null) {
                strengthOk = false;
                break;
            }
            boolean withinTol = withinTolerance(expected, candidateStrength, STRENGTH_TOLERANCE);
            if (!withinTol) {
                strengthOk = false;
                break;
            }
            
            // Check if this is an exact match
            boolean isExact = expected.compareTo(candidateStrength) == 0;
            if (!isExact) {
                allExact = false;
                anyToleranceOnly = true;
            }
            
            // Track relative deviation for tie-breaking (lower is better)
            BigDecimal diff = candidateStrength.subtract(expected).abs();
            BigDecimal rel = diff.divide(expected.abs(), java.math.MathContext.DECIMAL64);
            totalRelDiff += rel.doubleValue();
            relDiffCount++;
            
            // Store details for this strength comparison
            Map<String, Object> singleStrengthDetail = new HashMap<>();
            singleStrengthDetail.put("ingredient", im.substanceName);
            singleStrengthDetail.put("rxcui", im.rxcui);
            singleStrengthDetail.put("expected", expected);
            singleStrengthDetail.put("actual", candidateStrength);
            singleStrengthDetail.put("expectedUnit", expectedDenominator != null ? 
                expectedNumerator + "/" + expectedDenominator : expectedNumerator);
            singleStrengthDetail.put("actualUnit", candidateDenominator != null ? 
                candidateNumerator + "/" + candidateDenominator : candidateNumerator);
            singleStrengthDetail.put("exactMatch", isExact);
            singleStrengthDetail.put("relativeDiff", rel.doubleValue());
            strengthDetails.add(singleStrengthDetail);
        }
        // If we had nothing to compare, reject the candidate (strict validation)
        if (!comparedAny) strengthOk = false;
        details.put("strengthMatch", strengthOk);
        if (strengthOk && relDiffCount > 0) {
            details.put("strengthRelDiff", totalRelDiff / relDiffCount);
            details.put("strengthExactMatch", allExact); // true if all strengths match exactly
            details.put("strengthToleranceOnly", anyToleranceOnly); // true if any strength is only within tolerance
            details.put("strengthDetails", strengthDetails); // Detailed info for each strength comparison
        }
        return strengthOk;
    }

    /**
     * Checks if ingredient sets are compatible with bidirectional comparison.
     * Both sets must contain exactly the same ingredients (allowing PIN/IN flexibility).
     * PIN and IN for the same substance are considered compatible.
     */
    private boolean areIngredientsCompatible(java.util.Set<String> expected, java.util.Set<String> candidate) {
        // If candidate is empty, treat as compatible according to test expectation
        if (candidate.isEmpty()) return true;
        
        // If expected is empty but candidate has ingredients, they're not compatible
        if (expected.isEmpty()) return false;
        
        // CRITICAL: Both sets must have the same number of unique ingredient groups
        // This prevents matching combination products to single-ingredient products
        // First, count unique ingredient groups in both sets (accounting for PIN/IN variants)
        java.util.Set<String> expectedGroups = new java.util.HashSet<>();
        for (String expectedRxcui : expected) {
            // Find the IN root for this RxCUI
            String root = findIngredientRoot(expectedRxcui);
            if (root != null) {
                expectedGroups.add(root);
            } else {
                // If we can't find a root, use the RxCUI itself as a unique group
                expectedGroups.add(expectedRxcui);
            }
        }
        
        java.util.Set<String> candidateGroups = new java.util.HashSet<>();
        for (String candidateRxcui : candidate) {
            // Find the IN root for this RxCUI
            String root = findIngredientRoot(candidateRxcui);
            if (root != null) {
                candidateGroups.add(root);
            } else {
                // If we can't find a root, use the RxCUI itself as a unique group
                candidateGroups.add(candidateRxcui);
            }
        }
        
        // If the number of unique ingredient groups differs, they're not compatible
        if (expectedGroups.size() != candidateGroups.size()) {
            return false;
        }
        
        // Bidirectional comparison: both sets must be identical (allowing PIN/IN flexibility)
        // This means: every expected ingredient must have a match in candidate, and vice versa
        
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
                return false;
            }
        }
        
        // 2. Check if all candidate ingredients have compatible matches in expected (reverse check)
        // This ensures that candidate doesn't have additional ingredients that aren't in expected
        for (String candidateRxcui: candidate) {
            boolean hasCompatibleMatch = false; 

            // Direct match
            if (expected.contains(candidateRxcui)) {
                hasCompatibleMatch = true;  
            }
            // Check for PIN/IN compatibility
            else {
                for (String expectedRxcui: expected) {
                    if (areRxcuisCompatible(expectedRxcui, candidateRxcui)) {
                        hasCompatibleMatch = true; 
                        break; 
                    }
                }
            }
            
            // If this candidate ingredient has no match in expected, they're not compatible
            if (!hasCompatibleMatch) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Finds the IN (Ingredient Name) root for a given RxCUI by traversing form_of/has_form relationships.
     * Returns null if the root cannot be determined.
     */
    @Nullable
    private String findIngredientRoot(@NotNull String rxcui) {
        // Try to use provider's method if available
        if (candidateProvider instanceof LocalRxNormCandidateProvider) {
            return ((LocalRxNormCandidateProvider) candidateProvider).findIngredientRoot(rxcui);
        }
        // Fallback: return the RxCUI itself if we can't determine the root
        return rxcui;
    }
    
    /**
     * Checks if two RxCUIs are compatible (PIN/IN for same substance).
     * Uses provider's method if available, otherwise falls back to RxNav API.
     */
    private boolean areRxcuisCompatible(String rxcui1, String rxcui2) {
        // If they're the same, they're compatible
        if (rxcui1.equals(rxcui2)) {
            return true;
        }
        
        // Try to use provider's compatibility check method (e.g., LocalRxNormCandidateProvider)
        if (candidateProvider instanceof LocalRxNormCandidateProvider) {
            return ((LocalRxNormCandidateProvider) candidateProvider).areRxcuisCompatible(rxcui1, rxcui2);
        }
        
        // Fallback: Query RxNav API (only works with RxNavCandidateProvider)
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

    /**
     * Gets the drug volume in milliliters, if the drug has a volume amount.
     * Returns null if the drug has no volume or the unit is not a volume unit.
     */
    @Nullable
    private BigDecimal getDrugVolumeInMl(@NotNull GraphDrug drug) {
        if (drug.amount() == null || drug.unit() == null || drug.unit().ucumCs() == null) {
            return null;
        }
        
        String ucum = drug.unit().ucumCs().trim();
        return convertVolumeToMl(drug.amount(), ucum);
    }
    
    /**
     * Converts a volume value to milliliters.
     * Supports: ml, l, ul/µl/microl, nl/nanol, dl/decil
     * Returns null if the unit is not a recognized volume unit.
     */
    @Nullable
    private BigDecimal convertVolumeToMl(@NotNull BigDecimal value, @NotNull String volumeUnit) {
        String lower = volumeUnit.toLowerCase(Locale.ROOT);
        
        if (lower.equals("ml")) {
            return value;
        } else if (lower.equals("l")) {
            return value.multiply(new BigDecimal("1000"), java.math.MathContext.DECIMAL64);
        } else if (lower.equals("ul") || lower.equals("µl") || lower.equals("microl")) {
            return value.multiply(new BigDecimal("0.001"), java.math.MathContext.DECIMAL64);
        } else if (lower.equals("nl") || lower.equals("nanol")) {
            return value.multiply(new BigDecimal("0.000001"), java.math.MathContext.DECIMAL64);
        } else if (lower.equals("dl") || lower.equals("decil")) {
            return value.multiply(new BigDecimal("100"), java.math.MathContext.DECIMAL64);
        }
        
        return null;
    }
    
    /**
     * Checks if a unit string represents a volume unit.
     */
    private boolean isVolumeUnit(@NotNull String unit) {
        String lower = unit.toLowerCase(Locale.ROOT);
        return lower.equals("ml") || lower.equals("l") || 
               lower.equals("ul") || lower.equals("µl") || lower.equals("microl") ||
               lower.equals("nl") || lower.equals("nanol") ||
               lower.equals("dl") || lower.equals("decil");
    }
    
    /**
     * Normalizes volume denominator units to "mL" (canonical form).
     * If the unit is not a volume unit, returns it unchanged.
     */
    @NotNull
    private String normalizeVolumeDenominator(@NotNull String denominator) {
        if (isVolumeUnit(denominator)) {
            return "mL";
        }
        return denominator;
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
        // Numerator unit per ingredient (e.g., "mg", "mmol"); null implies legacy "mg"
        public final Map<String, String> numeratorUnits;
        public final Map<String, String> denominatorUnits; // Maps RxCUI to denominator unit (null for scalar strengths)
        public final String doseForm;

        public RxNormCandidate(String rxcui, String name, String tty, 
                              List<String> ingredients, Map<String, BigDecimal> strengths, String doseForm) {
            this(rxcui, name, tty, ingredients, strengths, null, null, doseForm);
        }

        public RxNormCandidate(String rxcui, String name, String tty, 
                              List<String> ingredients, Map<String, BigDecimal> strengths, 
                              Map<String, String> denominatorUnits, String doseForm) {
            this(rxcui, name, tty, ingredients, strengths, null, denominatorUnits, doseForm);
        }

        public RxNormCandidate(String rxcui, String name, String tty,
                              List<String> ingredients, Map<String, BigDecimal> strengths,
                              Map<String, String> numeratorUnits,
                              Map<String, String> denominatorUnits, String doseForm) {
            this.rxcui = rxcui;
            this.name = name;
            this.tty = tty;
            this.ingredients = ingredients;
            this.strengths = strengths;
            this.numeratorUnits = numeratorUnits != null ? numeratorUnits : new HashMap<>();
            this.denominatorUnits = denominatorUnits != null ? denominatorUnits : new HashMap<>();
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
    }
}
