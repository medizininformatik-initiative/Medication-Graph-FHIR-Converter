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
 * 
 * Uses RxNav REST API to find SCD/SBD candidates for German pharmaceutical products.
 * Includes caching to reduce API calls and improve performance.
 */
public final class RxNavCandidateProvider implements RxNormProductMatcher.RxNormCandidateProvider {

    private static final String RXNAV_BASE_URL = "https://rxnav.nlm.nih.gov/REST";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_DELAY = Duration.ofMillis(500);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    // Simple in-memory caches to reduce API calls
    private final Map<String, String> ttyCache = new ConcurrentHashMap<>();
    private final Map<String, List<RxNormProductMatcher.RxNormCandidate>> relatedCache = new ConcurrentHashMap<>();

    public RxNavCandidateProvider() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @NotNull
    public List<RxNormProductMatcher.RxNormCandidate> findScdCandidates(
            @NotNull List<RxNormProductMatcher.IngredientMatch> ingredients, 
            @NotNull String doseForm) {
        
        if (ingredients.isEmpty()) {
            return Collections.emptyList();
        }

        List<RxNormProductMatcher.RxNormCandidate> candidates = new ArrayList<>();
        
        // Strategy 1: Find SCDs related to each ingredient
        for (RxNormProductMatcher.IngredientMatch ingredient : ingredients) {
            List<RxNormProductMatcher.RxNormCandidate> relatedCandidates = 
                    findRelatedCandidates(ingredient.rxcui, "SCD");
            candidates.addAll(relatedCandidates);
        }
        //Debugging
        System.out.println("RxNav SCD related-candidates count: " + candidates.size());
        if (!candidates.isEmpty()) {
            RxNormProductMatcher.RxNormCandidate c = candidates.get(0);
            System.out.println("First related Rxcui-SCD-candidate: rxcui=" + c.rxcui +
                    ", tty=" + c.tty + ", doseForm='" + c.doseForm + "'" +
                    ", ingredients.size=" + (c.ingredients==null?0:c.ingredients.size()) +
                    ", strengths.size=" + (c.strengths==null?0:c.strengths.size()));
        }
        
        // Strategy 2: If no candidates found, try approximate search
        // COMMENTED OUT: Only search via RxCUI, not by approximate search
        /*
        if (candidates.isEmpty()) {
            String searchQuery = buildSearchQuery(ingredients, doseForm);
            List<RxNormProductMatcher.RxNormCandidate> searchCandidates = 
                    findCandidatesBySearch(searchQuery);
            candidates.addAll(searchCandidates);
            //Debugging
            System.out.println("RxNav SCD approx-candidates count: " + searchCandidates.size() +
                    " (query='" + searchQuery + "')");
            if (!searchCandidates.isEmpty()) {
                RxNormProductMatcher.RxNormCandidate c = searchCandidates.get(0);
                System.out.println("First approx candidate: rxcui=" + c.rxcui +
                        ", tty=" + c.tty + ", doseForm='" + c.doseForm + "'" +
                        ", ingredients.size=" + (c.ingredients==null?0:c.ingredients.size()) +
                        ", strengths.size=" + (c.strengths==null?0:c.strengths.size()));
            }
        }
        */
        
        // Compare dose forms: Relaxed filtering: keep candidates with empty/unknown dose form; the matcher validates strictly later
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
                        
                        // Check for extended/delayed release compatibility
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

    @Override
    @NotNull
    public List<RxNormProductMatcher.RxNormCandidate> findSbdCandidates(
            @NotNull GraphDrug drug, 
            @NotNull RxNormProductMatcher.MatchResult scdBaseMatch) {
        
        // Find SBDs related to the SCD
        List<RxNormProductMatcher.RxNormCandidate> sbdCandidates = 
                findRelatedCandidates(scdBaseMatch.rxcui, "SBD");
        
        // TODO: Add brand/manufacturer filtering based on drug information
        // For now, return all SBD candidates related to the SCD
        
        return sbdCandidates.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Finds candidates related to a given RXCUI with specified TTY.
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
     * Finds candidates using approximate search.
     */
    private List<RxNormProductMatcher.RxNormCandidate> findCandidatesBySearch(String query) {
        try {
            String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
            String url = RXNAV_BASE_URL + "/approximateTerm?term=" + encodedQuery + "&maxEntries=20&format=json";
            System.out.println("[RxNav] findCandidatesBySearch called: query='" + query + "', url=" + url);
            JsonNode response = makeApiCall(url);
            
            List<RxNormProductMatcher.RxNormCandidate> candidates = new ArrayList<>();
            
            if (response.has("approximateGroup") && response.get("approximateGroup").has("candidate")) {
                JsonNode candidatesNode = response.get("approximateGroup").get("candidate");
                
                for (JsonNode candidate : candidatesNode) {
                    if (candidate.has("rxcui")) {
                        String rxcui = candidate.get("rxcui").asText();
                        String name = candidate.has("name") ? candidate.get("name").asText() : "";
                        
                        // Try to get more details about this candidate
                        RxNormProductMatcher.RxNormCandidate detailedCandidate = 
                                getCandidateDetails(rxcui, name);
                        if (detailedCandidate != null) {
                            candidates.add(detailedCandidate);
                        }
                    }
                }
            }
            
            System.out.println("[RxNav] findCandidatesBySearch result: query='" + query + "', candidates=" + candidates.size());
            return candidates;
            
        } catch (Exception e) {
            System.err.println("[RxNav] Error in approximate search for '" + query + "': " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Gets detailed information about a candidate by RXCUI.
     */
    private RxNormProductMatcher.RxNormCandidate getCandidateDetails(String rxcui, String name) {
        try {
            String url = RXNAV_BASE_URL + "/rxcui/" + rxcui + "/properties?format=json";
            JsonNode response = makeApiCall(url);
            
            if (response.has("properties")) {
                JsonNode props = response.get("properties");
                String tty = props.has("tty") ? props.get("tty").asText() : "";
                String fullName = props.has("name") ? props.get("name").asText() : name;
                
                // For SCD/SBD, fetch ingredients from related IN/PIN and extract dose form/strengths from name
                List<String> ingredients = fetchIngredientRxcuis(rxcui);
                Map<String, BigDecimal> strengths = extractStrengths(fullName, ingredients);
                
                // Use structured dose form extraction (SCDF) instead of heuristic parsing
                String doseForm = extractDoseFormFromSCDF(rxcui);
                if (doseForm.isEmpty()) {
                    // Fallback to heuristic parsing if SCDF method fails
                    doseForm = extractDoseForm(fullName);
                }
                
                return new RxNormProductMatcher.RxNormCandidate(
                        rxcui, fullName, tty, ingredients, strengths, doseForm);
            }
            
        } catch (Exception e) {
            System.err.println("Error getting details for RXCUI " + rxcui + ": " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Parses a candidate from a concept node in the API response.
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
            Map<String, BigDecimal> strengths = extractStrengths(name, ingredients);
            
            // Use structured dose form extraction (SCDF) instead of heuristic parsing
            String doseForm = extractDoseFormFromSCDF(rxcui);
            if (doseForm.isEmpty()) {
                // Fallback to heuristic parsing if SCDF method fails
                doseForm = extractDoseForm(name);
            }
            
            return new RxNormProductMatcher.RxNormCandidate(
                    rxcui, name, tty, ingredients, strengths, doseForm);
                    
        } catch (Exception e) {
            System.err.println("Error parsing candidate: " + e.getMessage());
            return null;
        }
    }

    /**
     * Builds a search query from ingredients and dose form.
     */
    private String buildSearchQuery(List<RxNormProductMatcher.IngredientMatch> ingredients, String doseForm) {
        StringBuilder query = new StringBuilder();
        
        for (int i = 0; i < ingredients.size(); i++) {
            if (i > 0) query.append(" ");
            query.append(ingredients.get(i).substanceName);
        }
        
        if (doseForm != null && !doseForm.isEmpty()) {
            query.append(" ").append(doseForm);
        }
        
        return query.toString();
    }

    /**
     * Extracts ingredient RXCUIs from a drug name (simplified parsing).
     */
    private List<String> extractIngredients(String name) {
        // Deprecated in favor of fetchIngredientRxcuis via API; keep as fallback (empty)
        return new ArrayList<>();
    }

    /**
     * Extracts strengths from a drug name (simplified parsing).
     */
    private Map<String, BigDecimal> extractStrengths(String name, List<String> ingredientRxcuis) {
        Map<String, BigDecimal> strengths = new HashMap<>();
        if (name == null || name.isBlank()) return strengths;
        
        // Very simple parsing: pick first numeric + unit token and normalize to mg when possible
        // Examples: "Aspirin 500 mg tablet", "Ibuprofen 0.4 g capsule", "X 250 microgram solution"
        String lower = name.toLowerCase(Locale.ROOT);
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+(?:[\\.,]\\d+)?)\\s*(mg|g|µg|mcg|ug|microgram|mcg\\/ml|mg\\/ml|mg\\/mg)");
        java.util.regex.Matcher m = p.matcher(lower);
        if (!m.find()) return strengths;
        String numStr = m.group(1).replace(',', '.');
        String unit = m.group(2);
        BigDecimal value;
        try {
            value = new BigDecimal(numStr);
        } catch (NumberFormatException e) {
            return strengths;
        }
        // Normalize: g→mg, µg/mcg/ug/microgram→mg (divide by 1000), mg stay, per-ml keep raw (will likely fail strict compare)
        BigDecimal mg = null;
        if (unit.equals("mg")) {
            mg = value;
        } else if (unit.equals("g")) {
            mg = value.multiply(new BigDecimal("1000"));
        } else if (unit.equals("µg") || unit.equals("mcg") || unit.equals("ug") || unit.equals("microgram")) {
            mg = value.divide(new BigDecimal("1000"), java.math.MathContext.DECIMAL64);
        } else if (unit.equals("mg/ml") || unit.equals("mcg/ml")) {
            // leave empty; requires per-ml handling; return empty to avoid false matches
            return strengths;
        }
        if (mg == null) return strengths;
        
        if (ingredientRxcuis != null && !ingredientRxcuis.isEmpty()) {
            // Assign parsed strength to first ingredient RXCUI as heuristic for single-ingredient SCDs
            strengths.put(ingredientRxcuis.get(0), mg);
        }
        return strengths;
    }

    /**
     * Extracts dose form from SCD using structured RxNav API data (SCD → SCDF).
     * Uses SCDF → DF relationship to get precise dose form from DF (Dose Form) concept.
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
     * This is the most precise method:gets dose form as atomic RxNorm concept.
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
     * Fallback: Extracts dose form from drug name (heuristic parsing).
     * Used when SCDF method fails.
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
     * Makes an HTTP API call with retry logic.
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
     * Fetches ingredient RXCUIs (IN/PIN) for a given SCD/SBD candidate.
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
     * Clears the internal caches.
     */
    public void clearCache() {
        ttyCache.clear();
        relatedCache.clear();
    }

    /**
     * Resolves an ingredient RXCUI by English substance name via RxNav.
     * Tries approximateTerm first, then picks top candidate RXCUI.
     */
    public String resolveIngredientRxcuiByName(String substanceName) {
        if (substanceName == null || substanceName.isBlank()) return null;
        try {
            String encoded = java.net.URLEncoder.encode(substanceName, java.nio.charset.StandardCharsets.UTF_8);
            String url = RXNAV_BASE_URL + "/approximateTerm?term=" + encoded + "&maxEntries=5&format=json";
            System.out.println("[RxNav] resolveIngredientRxcuiByName called: substanceName='" + substanceName + "', url=" + url);
            JsonNode response = makeApiCall(url);
            if (response.has("approximateGroup") && response.get("approximateGroup").has("candidate")) {
                JsonNode candidates = response.get("approximateGroup").get("candidate");
                for (JsonNode c : candidates) {
                    if (c.has("rxcui")) {
                        String rxcui = c.get("rxcui").asText();
                        System.out.println("[RxNav] resolveIngredientRxcuiByName result: substanceName='" + substanceName + "', rxcui=" + rxcui);
                        return rxcui;
                    }
                }
            }
            System.out.println("[RxNav] resolveIngredientRxcuiByName result: substanceName='" + substanceName + "', rxcui=null (no candidates)");
        } catch (Exception e) {
            System.out.println("[RxNav] resolveIngredientRxcuiByName error: substanceName='" + substanceName + "', error=" + e.getMessage());
        }
        return null;
    }
}
