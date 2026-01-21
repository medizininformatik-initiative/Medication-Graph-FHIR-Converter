package de.medizininformatikinitiative.medgraph.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.medizininformatikinitiative.medgraph.fhirexporter.exporter.Neo4jProductExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.*;
import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase;
import org.neo4j.driver.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Performance-Test für den SCD-Matching-Algorithmus.
 * 
 * Führt den Matching-Algorithmus mehrfach aus und misst die Laufzeit.
 * Berechnet Statistiken wie Durchschnitt, Min, Max, Median.
 */
public class PerformanceTestSCDMatching {
    
    public static void main(String[] args) {
        String uri = args.length > 0 ? args[0] : "bolt://localhost:7687";
        String user = args.length > 1 ? args[1] : "neo4j";
        String password = args.length > 2 ? args[2] : "7o7MP~8_)h~0";
        int iterations = args.length > 3 ? Integer.parseInt(args[3]) : 10;
        String outputFile = args.length > 4 ? args[4] : "medgraph/scd_matching_performance.json";
        
        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
             Session session = driver.session()) {
            
            System.out.println("=== Performance-Test: SCD Matching ===\n");
            System.out.println("Anzahl Durchläufe: " + iterations);
            System.out.println("Ausgabe-Datei: " + outputFile);
            System.out.println();
            
            // Initialize matcher (einmalig, außerhalb der Zeitmessung)
            System.out.println("Initialisiere Matcher...");
            long initStart = System.currentTimeMillis();
            Neo4jCypherDatabase database = new Neo4jCypherDatabase(session);
            DoseFormMapper.initialize(database);
            RxNormMatcherSetup.initializeWithLocalProviders(database);
            RxNormProductMatcher matcher = RxNormMatcherSetup.createMatcher();
            long initTime = System.currentTimeMillis() - initStart;
            System.out.println("  Initialisierung abgeschlossen (" + initTime + " ms)\n");
            
            // Load products (einmalig, außerhalb der Zeitmessung)
            System.out.println("Lade Products aus Neo4j...");
            long loadStart = System.currentTimeMillis();
            Neo4jProductExporter exporter = new Neo4jProductExporter(session, false);
            List<GraphProduct> products = exporter.exportObjects().collect(Collectors.toList());
            long loadTime = System.currentTimeMillis() - loadStart;
            System.out.println("  Gefunden: " + products.size() + " Products (" + loadTime + " ms)\n");
            
            // Prepare drugs list (einmalig)
            List<GraphDrug> allDrugs = new ArrayList<>();
            for (GraphProduct product : products) {
                allDrugs.addAll(product.drugs());
            }
            System.out.println("Gesamt: " + allDrugs.size() + " Drugs\n");
            
            // Warm-up: Ein Durchlauf zum Aufwärmen des Caches (wird nicht gezählt)
            System.out.println("Warm-up: Aufwärmen des Caches...");
            long warmupStart = System.currentTimeMillis();
            runMatching(matcher, allDrugs);
            long warmupTime = System.currentTimeMillis() - warmupStart;
            System.out.println("  Warm-up abgeschlossen (" + warmupTime + " ms)\n");
            
            // Performance-Test: Mehrere Durchläufe (nach Warm-up)
            List<Long> executionTimes = new ArrayList<>();
            List<Long> matchCounts = new ArrayList<>();
            
            System.out.println("Starte Performance-Test (mit warmem Cache)...");
            System.out.println("=".repeat(70));
            
            for (int i = 0; i < iterations; i++) {
                System.out.print("Durchlauf " + (i + 1) + "/" + iterations + ": ");
                
                long startTime = System.currentTimeMillis();
                int matchCount = runMatching(matcher, allDrugs);
                long endTime = System.currentTimeMillis();
                
                long executionTime = endTime - startTime;
                executionTimes.add(executionTime);
                matchCounts.add((long) matchCount);
                
                System.out.println(executionTime + " ms (" + matchCount + " Matches)");
            }
            
            System.out.println("=".repeat(70));
            System.out.println();
            
            // Calculate statistics
            Collections.sort(executionTimes);
            long minTime = executionTimes.get(0);
            long maxTime = executionTimes.get(executionTimes.size() - 1);
            double avgTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
            
            // Print results
            printResults(executionTimes, matchCounts, initTime, loadTime, warmupTime, minTime, maxTime, avgTime);
            
            // Save to JSON
            saveResultsToJson(executionTimes, matchCounts, initTime, loadTime, warmupTime, minTime, maxTime, avgTime, 
                            products.size(), allDrugs.size(), iterations, outputFile);
            System.out.println("\nErgebnisse gespeichert in: " + outputFile);
            
        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Führt das SCD-Matching für alle Drugs durch und gibt die Anzahl der Matches zurück.
     */
    private static int runMatching(RxNormProductMatcher matcher, List<GraphDrug> drugs) {
        int matchCount = 0;
        
        for (GraphDrug drug : drugs) {
            RxNormProductMatcher.MatchResult match = matcher.matchSCD(drug);
            if (match != null) {
                matchCount++;
            }
        }
        
        return matchCount;
    }
    
    private static void printResults(List<Long> executionTimes, List<Long> matchCounts,
                                    long initTime, long loadTime, long warmupTime,
                                    long minTime, long maxTime, double avgTime) {
        System.out.println("=".repeat(70));
        System.out.println("PERFORMANCE-ERGEBNISSE (WARM CACHE)");
        System.out.println("=".repeat(70));
        System.out.println();
        
        System.out.println("Initialisierung:");
        System.out.println("  Matcher-Initialisierung: " + initTime + " ms");
        System.out.println("  Daten-Laden:            " + loadTime + " ms");
        System.out.println("  Warm-up Durchlauf:     " + warmupTime + " ms (nicht in Statistik enthalten)");
        System.out.println();
        
        System.out.println("Matching-Performance (" + executionTimes.size() + " Durchläufe):");
        System.out.println("  Minimale Laufzeit:       " + String.format("%,d", minTime) + " ms");
        System.out.println("  Maximale Laufzeit:       " + String.format("%,d", maxTime) + " ms");
        System.out.println("  Durchschnittliche Laufzeit: " + String.format("%.2f", avgTime) + " ms");
        System.out.println();
        
        System.out.println("Einzelne Durchläufe:");
        for (int i = 0; i < executionTimes.size(); i++) {
            System.out.println("  Durchlauf " + (i + 1) + ": " + String.format("%,d", executionTimes.get(i)) + 
                             " ms (" + matchCounts.get(i) + " Matches)");
        }
        System.out.println();
        
        // Calculate standard deviation
        double variance = executionTimes.stream()
                .mapToDouble(time -> Math.pow(time - avgTime, 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);
        System.out.println("Statistik:");
        System.out.println("  Standardabweichung:     " + String.format("%.2f", stdDev) + " ms");
        System.out.println("  Variationskoeffizient:  " + String.format("%.2f", (stdDev / avgTime * 100)) + "%");
        System.out.println("=".repeat(70));
    }
    
    private static void saveResultsToJson(List<Long> executionTimes, List<Long> matchCounts,
                                         long initTime, long loadTime, long warmupTime,
                                         long minTime, long maxTime, double avgTime,
                                         int totalProducts, int totalDrugs, int iterations,
                                         String filename) throws IOException {
        // Ensure directory exists
        Path filePath = Paths.get(filename);
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        Map<String, Object> jsonData = new LinkedHashMap<>();
        
        // Initialization times
        Map<String, Object> initialization = new LinkedHashMap<>();
        initialization.put("matcherInitialization", initTime);
        initialization.put("dataLoading", loadTime);
        initialization.put("warmupTime", warmupTime);
        initialization.put("note", "Warm-up Durchlauf wurde durchgeführt, um den Cache aufzuwärmen");
        jsonData.put("initialization", initialization);
        
        // Execution statistics
        Map<String, Object> statistics = new LinkedHashMap<>();
        statistics.put("iterations", iterations);
        statistics.put("minTime", minTime);
        statistics.put("maxTime", maxTime);
        statistics.put("averageTime", avgTime);
        
        // Calculate standard deviation
        double variance = executionTimes.stream()
                .mapToDouble(time -> Math.pow(time - avgTime, 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);
        statistics.put("standardDeviation", stdDev);
        statistics.put("coefficientOfVariation", stdDev / avgTime * 100);
        
        jsonData.put("statistics", statistics);
        
        // Individual runs
        List<Map<String, Object>> runs = new ArrayList<>();
        for (int i = 0; i < executionTimes.size(); i++) {
            Map<String, Object> run = new LinkedHashMap<>();
            run.put("iteration", i + 1);
            run.put("executionTime", executionTimes.get(i));
            run.put("matchCount", matchCounts.get(i));
            runs.add(run);
        }
        jsonData.put("runs", runs);
        
        // Dataset info
        Map<String, Object> dataset = new LinkedHashMap<>();
        dataset.put("totalProducts", totalProducts);
        dataset.put("totalDrugs", totalDrugs);
        jsonData.put("dataset", dataset);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(jsonData, writer);
        }
    }
}

