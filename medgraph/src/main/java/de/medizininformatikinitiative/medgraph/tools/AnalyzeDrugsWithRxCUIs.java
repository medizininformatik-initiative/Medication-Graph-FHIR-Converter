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
 * Analysiert: Von allen Drugs mit bekannten aktiven Ingredients (RxCUIs),
 * wie viele erhalten mindestens einen passenden RxNorm-SCD?
 * 
 * Diese Analyse filtert:
 * 1. Drugs mit aktiven Ingredients
 * 2. Diese aktiven Ingredients müssen RxCUIs haben
 * 3. Dann wird geprüft, wie viele davon erfolgreich matchen
 */
public class AnalyzeDrugsWithRxCUIs {
    
    public static void main(String[] args) {
        String uri = args.length > 0 ? args[0] : "bolt://localhost:7687";
        String user = args.length > 1 ? args[1] : "neo4j";
        String password = args.length > 2 ? args[2] : System.getenv().getOrDefault("NEO4J_PASSWORD", "");
        if (password.isEmpty()) {
            System.err.println("WARNING: No Neo4j password provided!");
            System.err.println("Please set NEO4J_PASSWORD environment variable or pass as command line argument.");
            System.exit(1);
        }
        String outputFile = args.length > 3 ? args[3] : "output/analysis/drugs_with_rxcuis_analysis.json";
        
        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
             Session session = driver.session()) {
            
            System.out.println("=== Analyse: Drugs mit RxCUIs und SCD Matching ===\n");
            
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
            
            // Statistics
            long totalDrugs = 0;
            long drugsWithActiveIngredients = 0;
            long drugsWithRxCUIs = 0;
            long drugsWithRxCUIsAndMatch = 0;
            
            System.out.println("Analysiere Drugs...");
            int processedProducts = 0;
            
            for (GraphProduct product : products) {
                processedProducts++;
                if (processedProducts % 100 == 0) {
                    System.out.println("  Verarbeitet: " + processedProducts + " / " + products.size() + " Products");
                }
                
                for (GraphDrug drug : product.drugs()) {
                    totalDrugs++;
                    
                    // Step 1: Check for active ingredients
                    List<GraphIngredient> activeIngredients = drug.ingredients().stream()
                            .filter(GraphIngredient::isActive)
                            .collect(Collectors.toList());
                    
                    if (activeIngredients.isEmpty()) {
                        continue; // Skip drugs without active ingredients
                    }
                    
                    drugsWithActiveIngredients++;
                    
                    // Step 2: Check if active ingredients have RxCUIs
                    boolean hasRxCUIs = activeIngredients.stream()
                            .filter(ing -> ing instanceof SimpleGraphIngredient)
                            .anyMatch(ing -> {
                                List<String> rxcuis = ((SimpleGraphIngredient) ing).getRxcuiCodes();
                                return rxcuis != null && !rxcuis.isEmpty();
                            });
                    
                    if (!hasRxCUIs) {
                        continue; // Skip drugs without RxCUIs
                    }
                    
                    drugsWithRxCUIs++;
                    
                    // Step 3: Try SCD matching
                    RxNormProductMatcher.MatchResult match = matcher.matchSCD(drug);
                    if (match != null) {
                        drugsWithRxCUIsAndMatch++;
                    }
                }
            }
            
            System.out.println("\nAnalyse abgeschlossen!\n");
            
            // Print results
            printResults(totalDrugs, drugsWithActiveIngredients, drugsWithRxCUIs, drugsWithRxCUIsAndMatch);
            
            // Save to JSON
            saveResultsToJson(totalDrugs, drugsWithActiveIngredients, drugsWithRxCUIs, drugsWithRxCUIsAndMatch, outputFile);
            System.out.println("\nErgebnisse gespeichert in: " + outputFile);
            
        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printResults(long totalDrugs, long drugsWithActiveIngredients, 
                                    long drugsWithRxCUIs, long drugsWithRxCUIsAndMatch) {
        System.out.println("=".repeat(70));
        System.out.println("ERGEBNISSE");
        System.out.println("=".repeat(70));
        System.out.println();
        
        System.out.println("Gesamt-Statistiken:");
        System.out.println("  Alle Drugs insgesamt:                    " + String.format("%,d", totalDrugs));
        System.out.println("  Drugs mit aktiven Ingredients:           " + String.format("%,d", drugsWithActiveIngredients) + 
                         " (" + String.format("%.1f", (double) drugsWithActiveIngredients / totalDrugs * 100) + "%)");
        System.out.println("  Drugs mit aktiven Ingredients + RxCUIs:  " + String.format("%,d", drugsWithRxCUIs) + 
                         " (" + String.format("%.1f", (double) drugsWithRxCUIs / totalDrugs * 100) + "%)");
        System.out.println();
        
        System.out.println("=".repeat(70));
        System.out.println("ANTWORT AUF DIE FRAGE:");
        System.out.println("=".repeat(70));
        System.out.println();
        System.out.println("Von allen Drugs mit bekannten aktiven Ingredients (RxCUIs):");
        System.out.println("  Anzahl: " + String.format("%,d", drugsWithRxCUIs));
        System.out.println();
        System.out.println("Wie viele erhalten mindestens einen passenden RxNorm-SCD?");
        System.out.println("  Anzahl: " + String.format("%,d", drugsWithRxCUIsAndMatch));
        System.out.println("  Anteil: " + String.format("%.2f", 
                drugsWithRxCUIs > 0 ? (double) drugsWithRxCUIsAndMatch / drugsWithRxCUIs * 100 : 0.0) + "%");
        System.out.println();
        System.out.println("=".repeat(70));
    }
    
    private static void saveResultsToJson(long totalDrugs, long drugsWithActiveIngredients,
                                         long drugsWithRxCUIs, long drugsWithRxCUIsAndMatch,
                                         String filename) throws IOException {
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
        
        Map<String, Object> jsonData = new LinkedHashMap<>();
        
        jsonData.put("totalDrugs", totalDrugs);
        jsonData.put("drugsWithActiveIngredients", drugsWithActiveIngredients);
        jsonData.put("drugsWithActiveIngredientsPercentage", 
                totalDrugs > 0 ? (double) drugsWithActiveIngredients / totalDrugs * 100 : 0.0);
        
        jsonData.put("drugsWithRxCUIs", drugsWithRxCUIs);
        jsonData.put("drugsWithRxCUIsPercentage", 
                totalDrugs > 0 ? (double) drugsWithRxCUIs / totalDrugs * 100 : 0.0);
        
        jsonData.put("drugsWithRxCUIsAndMatch", drugsWithRxCUIsAndMatch);
        jsonData.put("drugsWithRxCUIsAndMatchPercentage", 
                drugsWithRxCUIs > 0 ? (double) drugsWithRxCUIsAndMatch / drugsWithRxCUIs * 100 : 0.0);
        
        // Answer to the question
        Map<String, Object> answer = new LinkedHashMap<>();
        answer.put("question", "Von allen Drugs mit bekannten aktiven Ingredients (RxCUIs), " +
                              "wie viele erhalten mindestens einen passenden RxNorm-SCD?");
        answer.put("totalDrugsWithRxCUIs", drugsWithRxCUIs);
        answer.put("drugsWithMatch", drugsWithRxCUIsAndMatch);
        answer.put("matchPercentage", 
                drugsWithRxCUIs > 0 ? (double) drugsWithRxCUIsAndMatch / drugsWithRxCUIs * 100 : 0.0);
        jsonData.put("answer", answer);
        
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

