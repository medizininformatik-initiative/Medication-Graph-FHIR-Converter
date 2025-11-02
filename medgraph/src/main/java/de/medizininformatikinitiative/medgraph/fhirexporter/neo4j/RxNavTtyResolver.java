package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RxNav API-based implementation of RxcuiTermTypeResolver.
 * 
 * Uses RxNav REST API to determine the Term Type (TTY) for a given RXCUI.
 * Includes caching to reduce API calls and improve performance.
 */
public final class RxNavTtyResolver implements RxNormProductMatcher.RxcuiTermTypeResolver {

    private static final String RXNAV_BASE_URL = "https://rxnav.nlm.nih.gov/REST";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_DELAY = Duration.ofMillis(500);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    // Cache to reduce API calls
    private final java.util.Map<String, String> ttyCache = new ConcurrentHashMap<>();

    public RxNavTtyResolver() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Nullable
    public String resolveTty(@NotNull String rxcui) {
        if (rxcui == null || rxcui.trim().isEmpty()) {
            return null;
        }

        return ttyCache.computeIfAbsent(rxcui, this::fetchTtyFromApi);
    }

    /**
     * Fetches the TTY for a given RXCUI from the RxNav API.
     */
    private String fetchTtyFromApi(String rxcui) {
        try {
            String url = RXNAV_BASE_URL + "/rxcui/" + rxcui + "/properties?format=json";
            JsonNode response = makeApiCall(url);
            
            if (response.has("properties")) {
                JsonNode properties = response.get("properties");
                if (properties.has("tty")) {
                    return properties.get("tty").asText();
                }
            }
            
            return null;
            
        } catch (Exception e) {
            // Log error and return null (will be handled gracefully by the matcher)
            System.err.println("Error resolving TTY for RXCUI " + rxcui + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Makes an HTTP API call with retry logic.
     */
    private JsonNode makeApiCall(String url) throws IOException, InterruptedException {
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
     * Clears the internal cache.
     */
    public void clearCache() {
        ttyCache.clear();
    }

    /**
     * Gets the current cache size (for monitoring purposes).
     */
    public int getCacheSize() {
        return ttyCache.size();
    }
}
