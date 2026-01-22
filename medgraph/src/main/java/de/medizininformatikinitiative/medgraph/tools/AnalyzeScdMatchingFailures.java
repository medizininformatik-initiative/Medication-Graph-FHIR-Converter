package de.medizininformatikinitiative.medgraph.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.medizininformatikinitiative.medgraph.fhirexporter.exporter.Neo4jProductExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.*;
import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase;
import org.neo4j.driver.*;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Analysiert die Fehlerklassen beim SCD Matching.
 * 
 * Erstellt Statistiken über:
 * - Anzahl Produkte insgesamt
 * - Anzahl erfolgreicher SCD Matches
 * - Fehlerklassen (Early Rejections vs. Candidate Validation Failures)
 */
public class AnalyzeScdMatchingFailures {
    
    public enum FailureReason {
        // Early Rejections
        NO_ACTIVE_INGREDIENTS("No active ingredients"),
        NO_VALID_INGREDIENT_MATCHES("No valid ingredient matches"),
        NO_RXNORM_DOSE_FORM_MAPPING("No RxNorm dose form mapping"),
        NO_SCD_CANDIDATES("No SCD candidates found"),
        
        // Candidate Validation Failures
        DOSE_FORM_MISMATCH("Dose form mismatch"),
        INGREDIENTS_MISMATCH("Ingredients mismatch"),
        STRENGTH_MISMATCH("Strength mismatch"),
        SCORE_TOO_LOW("Score too low"),
        
        // Success
        SUCCESS("Success");
        
        private final String description;
        
        FailureReason(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public boolean isEarlyRejection() {
            return this == NO_ACTIVE_INGREDIENTS ||
                   this == NO_VALID_INGREDIENT_MATCHES ||
                   this == NO_RXNORM_DOSE_FORM_MAPPING ||
                   this == NO_SCD_CANDIDATES;
        }
        
        public boolean isCandidateValidationFailure() {
            return this == DOSE_FORM_MISMATCH ||
                   this == INGREDIENTS_MISMATCH ||
                   this == STRENGTH_MISMATCH ||
                   this == SCORE_TOO_LOW;
        }
    }
    
    private static class MatchingStatistics {
        long totalProducts = 0;
        long totalDrugs = 0;
        long successfulMatches = 0;
        Map<FailureReason, Long> failureCounts = new HashMap<>();
        
        void incrementFailure(FailureReason reason) {
            failureCounts.put(reason, failureCounts.getOrDefault(reason, 0L) + 1);
        }
    }
    
    public static void main(String[] args) {
        // Read from environment variables first, then command line args, then defaults
        String uri = args.length > 0 ? args[0] : System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost:7687");
        String user = args.length > 1 ? args[1] : System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        String password = args.length > 2 ? args[2] : System.getenv().getOrDefault("NEO4J_PASSWORD", "");
        String outputFile = args.length > 3 ? args[3] : "output/analysis/scd_matching_statistics.json";
        
        if (password.isEmpty()) {
            System.err.println("WARNING: No Neo4j password provided!");
            System.err.println("Please set NEO4J_PASSWORD environment variable or pass as command line argument.");
            System.exit(1);
        }
        
        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
             Session session = driver.session()) {
            
            System.out.println("=== SCD Matching Failure Analysis ===\n");
            
            // Initialize matcher
            Neo4jCypherDatabase database = new Neo4jCypherDatabase(session);
            DoseFormMapper.initialize(database);
            RxNormMatcherSetup.initializeWithLocalProviders(database);
            RxNormProductMatcher matcher = RxNormMatcherSetup.createMatcher();
            
            // Load products
            System.out.println("Lade Products aus Neo4j...");
            Neo4jProductExporter exporter = new Neo4jProductExporter(session, false);
            List<GraphProduct> products = exporter.exportObjects().collect(Collectors.toList());
            System.out.println("  Gefunden: " + products.size() + " Products\n");
            
            // Analyze matching
            MatchingStatistics stats = new MatchingStatistics();
            stats.totalProducts = products.size();
            
            System.out.println("Analysiere SCD Matching...");
            int processed = 0;
            for (GraphProduct product : products) {
                processed++;
                if (processed % 100 == 0) {
                    System.out.println("  Verarbeitet: " + processed + " / " + products.size());
                }
                
                for (GraphDrug drug : product.drugs()) {
                    stats.totalDrugs++;
                    FailureReason reason = analyzeDrugMatching(matcher, drug);
                    if (reason == FailureReason.SUCCESS) {
                        stats.successfulMatches++;
                    } else {
                        stats.incrementFailure(reason);
                    }
                }
            }
            
            System.out.println("\nAnalyse abgeschlossen!\n");
            
            // Print statistics
            printStatistics(stats);
            
            // Save to JSON
            saveStatisticsToJson(stats, outputFile);
            System.out.println("\nStatistiken gespeichert in: " + outputFile);
            
        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    private static void printStatistics(MatchingStatistics stats) {
        System.out.println("=".repeat(60));
        System.out.println("SCD MATCHING STATISTIKEN");
        System.out.println("=".repeat(60));
        System.out.printf("Anzahl Produkte insgesamt:     %10d%n", stats.totalProducts);
        System.out.printf("Anzahl Drugs insgesamt:         %10d%n", stats.totalDrugs);
        System.out.printf("Anzahl erfolgreiche Matches:    %10d%n", stats.successfulMatches);
        System.out.printf("Match-Rate:                     %10.2f%%%n", 
                stats.totalDrugs > 0 ? (double) stats.successfulMatches / stats.totalDrugs * 100.0 : 0.0);
        System.out.println("=".repeat(60));
        System.out.println("\nFEHLERKLASSEN:");
        System.out.println("-".repeat(60));
        
        // Group by failure type
        System.out.println("\nEarly Rejections:");
        for (FailureReason reason : FailureReason.values()) {
            if (reason.isEarlyRejection()) {
                long count = stats.failureCounts.getOrDefault(reason, 0L);
                System.out.printf("  %-35s %10d%n", reason.getDescription() + ":", count);
            }
        }
        
        System.out.println("\nCandidate Validation Failures:");
        for (FailureReason reason : FailureReason.values()) {
            if (reason.isCandidateValidationFailure()) {
                long count = stats.failureCounts.getOrDefault(reason, 0L);
                System.out.printf("  %-35s %10d%n", reason.getDescription() + ":", count);
            }
        }
        
        System.out.println("=".repeat(60));
    }
    
    /**
     * Analysiert das Matching für ein Drug und gibt den FailureReason zurück.
     * Führt den Matching-Prozess Schritt für Schritt durch und erfasst die Log-Ausgabe.
     */
    private static FailureReason analyzeDrugMatching(RxNormProductMatcher matcher, GraphDrug drug) {
        // Capture console output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream captureStream = new PrintStream(baos);
        System.setOut(captureStream);
        
        try {
            // Step 1: Check for active ingredients
            List<GraphIngredient> activeIngredients = drug.ingredients().stream()
                    .filter(GraphIngredient::isActive)
                    .collect(Collectors.toList());
            
            if (activeIngredients.isEmpty()) {
                return FailureReason.NO_ACTIVE_INGREDIENTS;
            }
            
            // Step 2: Try matching and capture output
            // The matcher will check ingredient matches AND dose form mapping internally
            // We rely on its log output to determine the failure reason
            RxNormProductMatcher.MatchResult result = matcher.matchSCD(drug);
            
            if (result != null) {
                return FailureReason.SUCCESS;
            }
            
            // Step 5: Parse output to determine validation failure
            String output = baos.toString();
            
            // WICHTIG: Prüfe zuerst die Log-Ausgabe des Matchers, bevor wir unsere eigenen Checks machen
            // Der Matcher gibt spezifische Rejection-Meldungen aus, die wir respektieren sollten
            
            // Check for "No valid ingredient matches" - das kann passieren, auch wenn RxCUIs vorhanden sind
            // z.B. wenn die Strength-Normalisierung fehlschlägt oder createIngredientMatch null zurückgibt
            if (output.contains("[Matcher] REJECTED: No valid ingredient matches")) {
                return FailureReason.NO_VALID_INGREDIENT_MATCHES;
            }
            
            // Check for "No RxNorm dose form mapping"
            if (output.contains("[Matcher] REJECTED: No RxNorm dose form mapping")) {
                return FailureReason.NO_RXNORM_DOSE_FORM_MAPPING;
            }
            
            // Check if NO_SCD_CANDIDATES happened
            // This should ONLY count cases where:
            // - ✅ Active ingredients exist
            // - ✅ Valid ingredient matches (RxCUIs) exist
            // - ✅ Dose form mapping exists
            // - ❌ But NO SCD RxCUIs found in RxNorm for these ingredients
            // 
            // The LocalRxNormCandidateProvider outputs this specific message when the SQL query
            // finds no SCDs containing all required ingredients (BEFORE dose form filtering)
            if (output.contains("[LocalRxNormCandidateProvider] DEBUG: No SCD RxCUIs found in RxNorm dump for these ingredients")) {
                return FailureReason.NO_SCD_CANDIDATES;
            }
            
            // Check for early dose form filtering (happens BEFORE validation)
            // The LocalRxNormCandidateProvider filters out candidates with mismatching dose forms early
            // This should be counted as DOSE_FORM_MISMATCH
            if (output.contains("[LocalRxNormCandidateProvider] DEBUG: All SCDs filtered out by early doseForm check")) {
                return FailureReason.DOSE_FORM_MISMATCH;
            }
            
            // Check for "No match" message which contains rejection counts
            // Format: "No match: X candidates, Y valid, Z doseForm, A ingredients, B strength"
            // This happens when candidates were found but failed validation
            if (output.contains("No match:")) {
                // Extract rejection counts using regex
                java.util.regex.Pattern doseFormPattern = java.util.regex.Pattern.compile("(\\d+)\\s+doseForm");
                java.util.regex.Pattern ingredientsPattern = java.util.regex.Pattern.compile("(\\d+)\\s+ingredients");
                java.util.regex.Pattern strengthPattern = java.util.regex.Pattern.compile("(\\d+)\\s+strength");
                
                java.util.regex.Matcher m;
                
                // Check dose form rejections first (checked first in validation)
                m = doseFormPattern.matcher(output);
                if (m.find()) {
                    int count = Integer.parseInt(m.group(1));
                    if (count > 0) {
                        return FailureReason.DOSE_FORM_MISMATCH;
                    }
                }
                
                // Check ingredients rejections
                m = ingredientsPattern.matcher(output);
                if (m.find()) {
                    int count = Integer.parseInt(m.group(1));
                    if (count > 0) {
                        return FailureReason.INGREDIENTS_MISMATCH;
                    }
                }
                
                // Check strength rejections
                m = strengthPattern.matcher(output);
                if (m.find()) {
                    int count = Integer.parseInt(m.group(1));
                    if (count > 0) {
                        return FailureReason.STRENGTH_MISMATCH;
                    }
                }
                
                // If we have "No match" but all rejection counts are 0, it means score too low
                // (candidates were found but didn't meet the minimum score threshold)
            }
            
            // If we have candidates but no match, it's likely a validation failure
            // Default to SCORE_TOO_LOW as catch-all for validation failures
            return FailureReason.SCORE_TOO_LOW;
            
        } finally {
            System.setOut(originalOut);
            try {
                captureStream.close();
                baos.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    private static void saveStatisticsToJson(MatchingStatistics stats, String filename) throws IOException {
        // Ensure directory exists
        Path filePath = Paths.get(filename);
        
        // If path is relative, resolve it relative to project root
        if (!filePath.isAbsolute()) {
            Path projectRoot = findProjectRoot();
            if (projectRoot != null) {
                filePath = projectRoot.resolve(filePath).normalize();
            }
        }
        
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("totalProducts", stats.totalProducts);
        jsonData.put("totalDrugs", stats.totalDrugs);
        jsonData.put("successfulMatches", stats.successfulMatches);
        jsonData.put("matchRate", stats.totalDrugs > 0 
                ? (double) stats.successfulMatches / stats.totalDrugs * 100.0 
                : 0.0);
        
        Map<String, Long> failures = new HashMap<>();
        for (FailureReason reason : FailureReason.values()) {
            if (reason != FailureReason.SUCCESS) {
                failures.put(reason.name(), stats.failureCounts.getOrDefault(reason, 0L));
            }
        }
        jsonData.put("failures", failures);
        
        // Add failure categories
        Map<String, Long> earlyRejections = new HashMap<>();
        Map<String, Long> validationFailures = new HashMap<>();
        
        for (FailureReason reason : FailureReason.values()) {
            long count = stats.failureCounts.getOrDefault(reason, 0L);
            if (reason.isEarlyRejection()) {
                earlyRejections.put(reason.name(), count);
            } else if (reason.isCandidateValidationFailure()) {
                validationFailures.put(reason.name(), count);
            }
        }
        
        jsonData.put("earlyRejections", earlyRejections);
        jsonData.put("validationFailures", validationFailures);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            gson.toJson(jsonData, writer);
        }
    }
    
    /**
     * Finds the project root directory by searching for build.gradle.kts or gradlew.
     *
     * @return Path to project root, or null if not found
     */
    private static Path findProjectRoot() {
        Path current = Paths.get(System.getProperty("user.dir"));
        Path root = current.getRoot();
        
        while (current != null && !current.equals(root)) {
            Path buildFile = current.resolve("build.gradle.kts");
            Path gradlew = current.resolve("gradlew");
            
            if (Files.exists(buildFile) || Files.exists(gradlew)) {
                return current;
            }
            
            current = current.getParent();
        }
        
        return null;
    }
    
}

