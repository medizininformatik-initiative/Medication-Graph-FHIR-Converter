package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * RxNav API-based implementation of RxNormCandidateProvider.
 * <p>
 * Uses the RxNav REST API to find SCD candidates for German pharmaceutical products.
 * Includes caching to reduce API calls and improve performance.
 * <p>
 * <b>Note:</b> This is the API-based implementation which is currently not used in production.
 * The system uses {@link LocalRxNormCandidateProvider} instead, which queries a local SQLite database dump.
 *
 * @author Lucy Strüfing
 */
public final class RxNavCandidateProvider implements RxNormProductMatcher.RxNormCandidateProvider {

    private static final String RXNAV_BASE_URL = "https://rxnav.nlm.nih.gov/REST";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_DELAY = Duration.ofMillis(500);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    // In-memory caches to reduce API calls
    private final Map<String, String> ttyCache = new ConcurrentHashMap<>();
    private final Map<String, List<RxNormProductMatcher.RxNormCandidate>> relatedCache = new ConcurrentHashMap<>();

    /**
     * Creates a new RxNav API-based candidate provider.
     */
    public RxNavCandidateProvider() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Finds SCD candidates using the RxNav API.
     * <p>
     * Searches for SCDs related to each ingredient RxCUI and filters by dose form.
     * Uses fuzzy matching for extended/delayed release variants.
     *
     * @param ingredients list of ingredient matches with RxCUIs
     * @param doseForm expected dose form for filtering
     * @return list of SCD candidates matching the criteria
     */
    @Override
    @NotNull
    public List<RxNormProductMatcher.RxNormCandidate> findScdCandidates(
            @NotNull List<RxNormProductMatcher.IngredientMatch> ingredients, 
            @NotNull String doseForm) {
        
        if (ingredients.isEmpty()) {
            return Collections.emptyList();
        }

        List<RxNormProductMatcher.RxNormCandidate> candidates = new ArrayList<>();
        
        // Find SCDs related to each ingredient RxCUI
        for (RxNormProductMatcher.IngredientMatch ingredient : ingredients) {
            List<RxNormProductMatcher.RxNormCandidate> relatedCandidates = 
                    findRelatedCandidates(ingredient.rxcui, "SCD");
            candidates.addAll(relatedCandidates);
        }
        
        System.out.println("[RxNav] Found " + candidates.size() + " SCD candidates");
        
        // Filter candidates by dose form (relaxed!: keep candidates with unknown dose form)
        System.out.println("[RxNav] Filtering candidates by dose form: expected='" + doseForm + "'");
        List<RxNormProductMatcher.RxNormCandidate> filteredCandidates = candidates.stream()
                .filter(candidate -> {
                    boolean exactMatch = candidate.doseForm == null || candidate.doseForm.isBlank() ||
                            doseForm.equalsIgnoreCase(candidate.doseForm);
                    
                    // Fuzzy matching for extended/delayed release variants
                    boolean fuzzyMatch = false;
                    if (!exactMatch && candidate.doseForm != null && doseForm != null) {
                        String expectedLower = doseForm.toLowerCase();
                        String candidateLower = candidate.doseForm.toLowerCase();
                        
                        if ((expectedLower.contains("delayed release") && candidateLower.contains("extended release")) ||
                            (expectedLower.contains("extended release") && candidateLower.contains("delayed release")) ||
                            (expectedLower.contains("retard") && candidateLower.contains("extended release")) ||
                            (expectedLower.contains("retard") && candidateLower.contains("delayed release"))) {
                            fuzzyMatch = true;
                        }
                    }
                    
                    boolean matches = exactMatch || fuzzyMatch;
                    System.out.println("[RxNav] Candidate rxcui=" + candidate.rxcui + 
                            ", doseForm='" + candidate.doseForm + "' => exact=" + exactMatch + 
                            ", fuzzy=" + fuzzyMatch + ", final=" + matches);
                    return matches;
                })
                .distinct()
                .collect(Collectors.toList());
        
        return filteredCandidates;
    }

    /**
     * Finds SBD candidates related to an SCD match using the RxNav API.
     *
     * @param drug the drug to match (currently not used for filtering)
     * @param scdBaseMatch the SCD match result to find related SBDs for
     * @return list of SBD candidates related to the SCD
     */
    @Override
    @NotNull
    public List<RxNormProductMatcher.RxNormCandidate> findSbdCandidates(
            @NotNull GraphDrug drug, 
            @NotNull RxNormProductMatcher.MatchResult scdBaseMatch) {
        
        List<RxNormProductMatcher.RxNormCandidate> sbdCandidates = 
                findRelatedCandidates(scdBaseMatch.rxcui, "SBD");
        
        return sbdCandidates.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Finds candidates related to a given RxCUI with specified TTY using the RxNav API.
     * Results are cached to avoid repeated API calls.
     *
     * @param rxcui the RxCUI to find related concepts for
     * @param tty the term type to filter by (e.g., "SCD", "SBD")
     * @return list of related candidates, empty if none found or error occurs
     */
    private List<RxNormProductMatcher.RxNormCandidate> findRelatedCandidates(String rxcui, String tty) {
        String cacheKey = rxcui + ":" + tty;
        
        return relatedCache.computeIfAbsent(cacheKey, key -> {
            try {
                String url = RXNAV_BASE_URL + "/rxcui/" + rxcui + "/related?tty=" + tty + "&format=json";
                System.out.println("[RxNav] findRelatedCandidates called: rxcui=" + rxcui + ", tty=" + tty + ", url=" + url);
                JsonNode response = makeApiCall(url);
                
                List<RxNormProductMatcher.RxNormCandidate> candidates = new ArrayList<>();
                
                if (response.has("relatedGroup") && response.get("relatedGroup").has("conceptGroup")) {
                    JsonNode conceptGroups = response.get("relatedGroup").get("conceptGroup");
                    
                    for (JsonNode group : conceptGroups) {
                        if (group.has("conceptProperties")) {
                            JsonNode concepts = group.get("conceptProperties");
                            
                            for (JsonNode concept : concepts) {
                                RxNormProductMatcher.RxNormCandidate candidate = 
                                        parseCandidateFromConcept(concept, tty);
                                if (candidate != null) {
                                    candidates.add(candidate);
                                }
                            }
                        }
                    }
                }
                
                System.out.println("[RxNav] findRelatedCandidates result: rxcui=" + rxcui + ", tty=" + tty + ", candidates=" + candidates.size());
                return candidates;
                
            } catch (Exception e) {
                // Log error and return empty list
                System.err.println("[RxNav] Error finding related candidates for " + rxcui + ": " + e.getMessage());
                return Collections.emptyList();
            }
        });
    }

    /**
     * Parses a candidate from a concept node in the RxNav API response.
     * Extracts RxCUI, name, TTY, ingredients, strengths, and dose form.
     *
     * @param concept the JSON concept node from the API response
     * @param expectedTty the expected term type (for validation)
     * @return parsed candidate, or null if parsing fails
     */
    private RxNormProductMatcher.RxNormCandidate parseCandidateFromConcept(JsonNode concept, String expectedTty) {
        try {
            String rxcui = concept.has("rxcui") ? concept.get("rxcui").asText() : "";
            String name = concept.has("name") ? concept.get("name").asText() : "";
            String tty = concept.has("tty") ? concept.get("tty").asText() : "";
            
            if (rxcui.isEmpty() || name.isEmpty()) {
                return null;
            }
            
            // Fetch ingredients via RxNav related (IN/PIN) and try to extract strengths from name
            List<String> ingredients = fetchIngredientRxcuis(rxcui);
            StrengthExtractionResult strengthData =
                extractComponentStrengths(rxcui, ingredients);
            if (strengthData.strengths().isEmpty()) {
                strengthData = extractStrengthsWithDenominators(name, ingredients);
            }
            Map<String, BigDecimal> strengths = strengthData.strengths();
            Map<String, String> numeratorUnits = strengthData.numeratorUnits();
            Map<String, String> denominatorUnits = strengthData.denominatorUnits();
            
            // Use structured dose form extraction (SCDF) instead of heuristic parsing
            String doseForm = extractDoseFormFromSCDF(rxcui);
            if (doseForm.isEmpty()) {
                // Fallback to heuristic parsing if SCDF method fails
                doseForm = extractDoseForm(name);
            }
            
            return new RxNormProductMatcher.RxNormCandidate(
                    rxcui, name, tty, ingredients, strengths, numeratorUnits, denominatorUnits, doseForm);
                    
        } catch (Exception e) {
            System.err.println("Error parsing candidate: " + e.getMessage());
            return null;
        }
    }

    private record StrengthExtractionResult(Map<String, BigDecimal> strengths,
                                            Map<String, String> numeratorUnits,
                                            Map<String, String> denominatorUnits) {}

    /**
     * Extracts component-specific strengths via SCDC concepts for a given SCD RxCUI.
     * <p>
     * Queries RxNav API for SCDC components and parses strengths from component names.
     * Returns strength maps per ingredient RxCUI. Falls back to empty maps if no components found.
     *
     * @param scdRxcui the SCD RxCUI to extract strengths for
     * @param ingredientRxcuis list of ingredient RxCUIs to match against
     * @return strength extraction result with strengths, numerator units, and denominator units
     */
    private StrengthExtractionResult extractComponentStrengths(String scdRxcui, List<String> ingredientRxcuis) {
        Map<String, BigDecimal> strengths = new HashMap<>();
        Map<String, String> numeratorUnits = new HashMap<>();
        Map<String, String> denominatorUnits = new HashMap<>();

        if (scdRxcui == null || ingredientRxcuis == null || ingredientRxcuis.isEmpty()) {
            return new StrengthExtractionResult(strengths, numeratorUnits, denominatorUnits);
        }

        Set<String> ingredientSet = new HashSet<>(ingredientRxcuis);

        try {
            String url = RXNAV_BASE_URL + "/rxcui/" + scdRxcui + "/related?tty=SCDC&format=json";
            JsonNode response = makeApiCall(url);
            if (response.has("relatedGroup") && response.get("relatedGroup").has("conceptGroup")) {
                JsonNode conceptGroups = response.get("relatedGroup").get("conceptGroup");
                for (JsonNode group : conceptGroups) {
                    if (!group.has("conceptProperties")) continue;
                    for (JsonNode component : group.get("conceptProperties")) {
                        if (!component.has("rxcui") || !component.has("name")) continue;
                        String componentRxcui = component.get("rxcui").asText();
                        String componentName = component.get("name").asText();

                        List<String> componentIngredients = fetchIngredientRxcuis(componentRxcui);
                        if (componentIngredients.isEmpty()) continue;

                        List<String> relevantIngredients = componentIngredients.stream()
                                .filter(ingredientSet::contains)
                                .collect(Collectors.toList());
                        if (relevantIngredients.isEmpty()) continue;

                        StrengthExtractionResult componentStrengths =
                                extractStrengthsWithDenominators(componentName, relevantIngredients);

                        for (String rxcui : relevantIngredients) {
                            BigDecimal value = componentStrengths.strengths().get(rxcui);
                            if (value != null) {
                                strengths.put(rxcui, value);
                                String num = componentStrengths.numeratorUnits().get(rxcui);
                                if (num != null) numeratorUnits.put(rxcui, num);
                                String den = componentStrengths.denominatorUnits().get(rxcui);
                                if (den != null) denominatorUnits.put(rxcui, den);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[RxNav] Error extracting component strengths for SCD " + scdRxcui + ": " + e.getMessage());
        }

        return new StrengthExtractionResult(strengths, numeratorUnits, denominatorUnits);
    }

    /**
     * Extracts strengths from a drug name using heuristic pattern matching.
     * <p>
     * Supports mass units (mg, g, µg) and molar units (mmol, mol, µmol),
     * including ratio denominators for volume/time/area (e.g., mg/mL, mg/h).
     * Normalizes units to canonical forms (mg, mmol, mL, etc.).
     *
     * @param name the drug name to parse
     * @param ingredientRxcuis list of ingredient RxCUIs to assign strengths to
     * @return strength extraction result with normalized strengths and units
     */
    private StrengthExtractionResult extractStrengthsWithDenominators(
            String name, List<String> ingredientRxcuis) {
        Map<String, BigDecimal> strengths = new HashMap<>();
        Map<String, String> numeratorUnits = new HashMap<>();
        Map<String, String> denominatorUnits = new HashMap<>();
        if (name == null || name.isBlank()) {
            return new StrengthExtractionResult(strengths, numeratorUnits, denominatorUnits);
        }
        
        // Enhanced parsing: supports ratio units (mg/mL, mg/h, etc.)
        // Examples: "Aspirin 500 mg", "X 10 mg/mL", "Y 2 mmol/L", "Z 100 mcg/h"
        String lower = name.toLowerCase(Locale.ROOT);
        // Capture number + unit, support optional /denominator
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
            "(\\d+(?:[\\.,]\\d+)?)\\s*(" +
                "(?:mg|g|µg|mcg|ug|microgram|mol|mmol|umol|µmol|μmol|nmol)" +
                "(?:\\/(?:ml|mL|l|h|hr|hour|d|day|cm2|cm²|24\\.h))?" +
            ")");
        java.util.regex.Matcher m = p.matcher(lower);
        if (!m.find()) {
            return new StrengthExtractionResult(strengths, numeratorUnits, denominatorUnits);
        }
        String numStr = m.group(1).replace(',', '.');
        String unit = m.group(2);
        BigDecimal value;
        try {
            value = new BigDecimal(numStr);
        } catch (NumberFormatException e) {
            return new StrengthExtractionResult(strengths, numeratorUnits, denominatorUnits);
        }
        
        // Check if this is a ratio unit (contains /)
        String denominatorUnit = null;
        String numeratorUnit = unit;
        int slashIdx = unit.indexOf('/');
        if (slashIdx > 0) {
            numeratorUnit = unit.substring(0, slashIdx);
            denominatorUnit = unit.substring(slashIdx + 1);
        }
        
        // Normalize numerator: mass → mg, molar → mmol
        BigDecimal normalizedValue = null;
        String normalizedNumeratorUnit = null;
        switch (numeratorUnit) {
            case "mg" -> {
                normalizedValue = value;
                normalizedNumeratorUnit = "mg";
            }
            case "g" -> {
                normalizedValue = value.multiply(new BigDecimal("1000"));
                normalizedNumeratorUnit = "mg";
            }
            case "µg", "mcg", "ug", "microgram" -> {
                normalizedValue = value.divide(new BigDecimal("1000"), java.math.MathContext.DECIMAL64);
                normalizedNumeratorUnit = "mg";
            }
            case "mmol" -> {
                normalizedValue = value;
                normalizedNumeratorUnit = "mmol";
            }
            case "mol" -> {
                normalizedValue = value.multiply(new BigDecimal("1000"));
                normalizedNumeratorUnit = "mmol";
            }
            case "umol", "µmol", "μmol" -> {
                normalizedValue = value.divide(new BigDecimal("1000"), java.math.MathContext.DECIMAL64);
                normalizedNumeratorUnit = "mmol";
            }
            case "nmol" -> {
                normalizedValue = value.divide(new BigDecimal("1000000"), java.math.MathContext.DECIMAL64);
                normalizedNumeratorUnit = "mmol";
            }
        }

        if (normalizedValue == null || normalizedNumeratorUnit == null) {
            return new StrengthExtractionResult(strengths, numeratorUnits, denominatorUnits);
        }

        // Denominator normalization
        if (denominatorUnit != null) {
            String d = normalizeDenominatorUnit(denominatorUnit);
            // Volume denominator: canonical mL (adjust value if original was L)
            if (d.equals("mL")) {
                // If original denominator was 'l'/'L', we already canonicalized by name
                // Adjust if original contained 'l' explicitly (scale down by 1000)
                if (denominatorUnit.equalsIgnoreCase("l")) {
                    normalizedValue = normalizedValue.divide(new BigDecimal("1000"), java.math.MathContext.DECIMAL64);
                }
                denominatorUnit = "mL";
            } else {
                denominatorUnit = d;
            }
        }
        
        if (ingredientRxcuis != null && !ingredientRxcuis.isEmpty()) {
            // Assign parsed strength to all ingredient RXCUIs (applies to single-ingredient SCDs and fallback for multi-ingredient)
            for (String rxcui : ingredientRxcuis) {
                strengths.put(rxcui, normalizedValue);
                numeratorUnits.put(rxcui, normalizedNumeratorUnit);
                if (denominatorUnit != null) {
                    denominatorUnits.put(rxcui, denominatorUnit);
                }
            }
        }
        return new StrengthExtractionResult(strengths, numeratorUnits, denominatorUnits);
    }
    
    /**
     * Normalizes denominator unit representation for consistent comparison.
     * Converts variants (e.g., "ml", "milliliter" → "mL", "hr", "hour" → "h").
     *
     * @param unit the unit string to normalize
     * @return normalized unit string
     */
    private String normalizeDenominatorUnit(String unit) {
        String l = unit.toLowerCase(Locale.ROOT);
        // Volume units -> mL
        if (l.equals("ml") || l.equals("milliliter")) return "mL";
        if (l.equals("l") || l.equals("liter") || l.equals("litre")) return "mL"; // Will be converted in normalization
        // Mass units (for mass/mass ratios like MG/MG) -> mg
        if (l.equals("mg") || l.equals("milligram")) return "mg";
        if (l.equals("g") || l.equals("gram")) return "mg"; // Will be normalized in ratio calculation
        // Time units -> keep as-is but normalize variants
        if (l.equals("hr") || l.equals("hour")) return "h";
        if (l.equals("day")) return "d";
        // Area units
        if (l.equals("cm²") || l.equals("cm2")) return "cm2";
        // Return as-is if unknown
        return l;
    }
    
    /**
     * Extracts strengths from a drug name (simplified parsing).
     * @deprecated Use extractStrengthsWithDenominators() instead
     */
    @Deprecated
    private Map<String, BigDecimal> extractStrengths(String name, List<String> ingredientRxcuis) {
        return extractStrengthsWithDenominators(name, ingredientRxcuis).strengths();
    }

    /**
     * Extracts dose form from SCD using structured RxNav API data.
     * <p>
     * Traverses the RxNorm hierarchy: SCD → SCDF → DF to get the precise dose form.
     * This is the preferred method as it uses structured RxNorm relationships.
     *
     * @param scdRxcui the SCD RxCUI to extract dose form for
     * @return normalized dose form string, or empty string if not found
     */
    private String extractDoseFormFromSCDF(String scdRxcui) {
        try {
            System.out.println("[RxNav] Extracting dose form from SCDF→DF for SCD: " + scdRxcui);
            
            // Step 1: Get SCDF related to this SCD
            String scdfUrl = RXNAV_BASE_URL + "/rxcui/" + scdRxcui + "/related?tty=SCDF&format=json";
            JsonNode scdfResponse = makeApiCall(scdfUrl);
            
            if (scdfResponse != null && scdfResponse.has("relatedGroup")) {
                JsonNode relatedGroup = scdfResponse.get("relatedGroup");
                if (relatedGroup.has("conceptGroup")) {
                    JsonNode conceptGroup = relatedGroup.get("conceptGroup");
                    
                    for (JsonNode group : conceptGroup) {
                        if (group.has("conceptProperties")) {
                            JsonNode conceptProperties = group.get("conceptProperties");
                            for (JsonNode concept : conceptProperties) {
                                if (concept.has("rxcui")) {
                                    String scdfRxcui = concept.get("rxcui").asText();
                                    String scdfName = concept.has("name") ? concept.get("name").asText() : "";
                                    System.out.println("[RxNav] Found SCDF: " + scdfName + " (RXCUI: " + scdfRxcui + ")");
                                    
                                    // Step 2: Get DF (Dose Form) related to this SCDF
                                    String doseForm = extractDoseFormFromDF(scdfRxcui);
                                    if (!doseForm.isEmpty()) {
                                        System.out.println("[RxNav] Dose form from SCDF→DF: " + doseForm);
                                        return doseForm;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            System.out.println("[RxNav] No SCDF found for SCD: " + scdRxcui);
            return "";
            
        } catch (Exception e) {
            System.err.println("[RxNav] Error extracting dose form from SCDF→DF: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Extracts dose form from SCDF using DF (Dose Form) relationship.
     * <p>
     * Gets the dose form as an atomic RxNorm concept, which is the most precise method.
     *
     * @param scdfRxcui the SCDF RxCUI to extract dose form for
     * @return normalized dose form string, or empty string if not found
     */
    private String extractDoseFormFromDF(String scdfRxcui) {
        try {
            System.out.println("[RxNav] Getting DF (Dose Form) for SCDF: " + scdfRxcui);
            
            // Get DF related to this SCDF
            String dfUrl = RXNAV_BASE_URL + "/rxcui/" + scdfRxcui + "/related?tty=DF&format=json";
            JsonNode dfResponse = makeApiCall(dfUrl);
            
            if (dfResponse != null && dfResponse.has("relatedGroup")) {
                JsonNode relatedGroup = dfResponse.get("relatedGroup");
                if (relatedGroup.has("conceptGroup")) {
                    JsonNode conceptGroup = relatedGroup.get("conceptGroup");
                    
                    for (JsonNode group : conceptGroup) {
                        if (group.has("conceptProperties")) {
                            JsonNode conceptProperties = group.get("conceptProperties");
                            for (JsonNode concept : conceptProperties) {
                                if (concept.has("name") && concept.has("tty") && "DF".equals(concept.get("tty").asText())) {
                                    String dfName = concept.get("name").asText();
                                    String dfRxcui = concept.has("rxcui") ? concept.get("rxcui").asText() : "";
                                    System.out.println("[RxNav] Found DF: " + dfName + " (RXCUI: " + dfRxcui + ")");
                                    
                                    // Return the dose form name (e.g., "Oral Tablet")
                                    return dfName.toLowerCase();
                                }
                            }
                        }
                    }
                }
            }
            
            System.out.println("[RxNav] No DF found for SCDF: " + scdfRxcui);
            return "";
            
        } catch (Exception e) {
            System.err.println("[RxNav] Error extracting dose form from DF: " + e.getMessage());
            return "";
        }
    }
    
    
    /**
     * Fallback method: Extracts dose form from drug name using heuristic pattern matching.
     * <p>
     * Used when the structured SCDF→DF method fails. Matches common dose form patterns
     * in the drug name (e.g., "oral tablet", "capsule", "injection").
     *
     * @param name the drug name to parse
     * @return normalized dose form string, or empty string if not found
     */
    private String extractDoseForm(String name) {
        System.out.println("[RxNav] Fallback: Extracting dose form from name: " + name);
        if (name == null) return "";
        String lower = name.toLowerCase(Locale.ROOT);

        // Extended Release / Delayed Release / Retard variants
        if (lower.contains("extended release oral tablet")) return "extended release oral tablet";
        if (lower.contains("delayed release oral tablet")) return "delayed release oral tablet";
        if (lower.contains("sustained release oral tablet")) return "sustained release oral tablet";
        if (lower.contains("retard oral tablet")) return "retard oral tablet";
        if (lower.contains("extended release tablet")) return "extended release tablet";
        if (lower.contains("delayed release tablet")) return "delayed release tablet";
        if (lower.contains("sustained release tablet")) return "sustained release tablet";
        if (lower.contains("retard tablet")) return "retard tablet";
        
        // Standard forms (hierarchical order)
        if (lower.contains("ophthalmic ointment")) return "ophthalmic ointment";
        if (lower.contains("ophthalmic solution")) return "ophthalmic solution";
        if (lower.contains("topical ointment")) return "topical ointment";
        if (lower.contains("injectable solution")) return "injectable solution";
        if (lower.contains("oral tablet")) return "oral tablet";
        if (lower.contains("tablet")) return "tablet";
        if (lower.contains("capsule")) return "capsule";
        if (lower.contains("cream")) return "cream";
        if (lower.contains("solution")) return "solution";
        if (lower.contains("ointment")) return "ointment";
        if (lower.contains("suspension")) return "suspension";
        return "";
    }

    /**
     * Makes an HTTP API call to RxNav with retry logic and rate limit handling.
     * <p>
     * Retries up to {@link #MAX_RETRIES} times.
     * Handles HTTP 429 (rate limit) responses by waiting before retrying.
     *
     * @param url the API URL to call
     * @return parsed JSON response
     * @throws IOException if the API call fails after all retries
     * @throws InterruptedException if the thread is interrupted during retry delays
     */
    public JsonNode makeApiCall(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .header("Accept", "application/json")
                .header("User-Agent", "medgraph-fhir-exporter/1.0")
                .build();
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return objectMapper.readTree(response.body());
                } else if (response.statusCode() == 429) {
                    // Rate limited - wait and retry
                    if (attempt < MAX_RETRIES) {
                        Thread.sleep(RETRY_DELAY.toMillis() * attempt);
                        continue;
                    }
                }
                
                throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
                
            } catch (Exception e) {
                if (attempt == MAX_RETRIES) {
                    throw e;
                }
                Thread.sleep(RETRY_DELAY.toMillis() * attempt);
            }
        }
        
        throw new IOException("Max retries exceeded");
    }

    /**
     * Fetches ingredient RxCUIs (IN/PIN) for a given SCD/SBD candidate via RxNav API.
     *
     * @param rxcui the SCD/SBD RxCUI to get ingredients for
     * @return list of ingredient RxCUIs, empty if not found or error occurs
     */
    private List<String> fetchIngredientRxcuis(String rxcui) {
        try {
            String url = RXNAV_BASE_URL + "/rxcui/" + rxcui + "/related?tty=IN+PIN&format=json";
            JsonNode response = makeApiCall(url);
            List<String> out = new ArrayList<>();
            if (response.has("relatedGroup") && response.get("relatedGroup").has("conceptGroup")) {
                JsonNode groups = response.get("relatedGroup").get("conceptGroup");
                for (JsonNode g : groups) {
                    if (g.has("conceptProperties")) {
                        for (JsonNode cp : g.get("conceptProperties")) {
                            if (cp.has("rxcui")) out.add(cp.get("rxcui").asText());
                        }
                    }
                }
            }
            return out;
        } catch (Exception e) {
            // Ignore and return empty list
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Clears all internal caches (TTY cache and related candidates cache).
     */
    public void clearCache() {
        ttyCache.clear();
        relatedCache.clear();
    }
}
