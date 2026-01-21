package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Analyzes SCD match coverage for the top 20 most frequently used active substances.
 * 
 * This class:
 * 1. Checks if the top 20 substances exist in Neo4j
 * 2. Finds all drugs containing these substances
 * 3. Checks which drugs have SCD matches
 * 4. Calculates coverage percentage
 * 
 * @author Lucy Strüfing
 */
public class TopSubstancesSCDCoverageAnalyzer {
    
    /**
     * Top 20 most frequently used active substances (Arzneiwirkstoffe).
     * 
     */
    private static final List<String> TOP_20_SUBSTANCES = List.of(
        "Ibuprofen",
        "Metamizol-Natrium",
        "Pantoprazol",
        "Levothyroxin-Natrium",
        "Ramipril",
        "Bisoprolol",
        "Candesartan",
        "Amlodipin",
        "Atorvastatin",
        "Metoprolol",
        "Torasemid",
        "Amoxicillin",
        "Salbutamol",
        "Simvastatin",
        "Prednisolon",
        "Metformin",
        "Diclofenac",
        "Acetylsalicylsäure",
        "Colecalciferol",
        "Beta-Lactamase-Inhibitoren"
    );
    
    private final Session session;
    private final RxNormProductMatcher matcher;
    
    public TopSubstancesSCDCoverageAnalyzer(Session session, RxNormProductMatcher matcher) {
        this.session = session;
        this.matcher = matcher;
    }
    
    /**
     * Runs the complete analysis and prints results.
     */
    public void analyze() {
        analyze(null);
    }
    
    /**
     * Runs the complete analysis and prints results.
     * @param jsonOutputFile Optional path to JSON output file. If null, no JSON is written.
     */
    public void analyze(String jsonOutputFile) {
        System.out.println("\n=== Top 20 Substances SCD Coverage Analysis ===\n");
        
        // Step 1: Check which substances exist in Neo4j
        Map<String, SubstanceInfo> substanceInfo = checkSubstancesInNeo4j();
        
        // Step 2: Find all drugs containing these substances
        Map<String, List<DrugInfo>> drugsBySubstance = findDrugsForSubstances(substanceInfo);
        
        // Step 3: Check SCD matches for each drug
        Map<String, CoverageResult> coverageResults = checkSCDMatches(drugsBySubstance);
        
        // Step 4: Print results
        printResults(substanceInfo, coverageResults);
        
        // Step 5: Save to JSON if requested
        if (jsonOutputFile != null && !jsonOutputFile.isBlank()) {
            try {
                saveResultsToJson(substanceInfo, coverageResults, jsonOutputFile);
                System.out.println("\nResults saved to: " + jsonOutputFile);
            } catch (IOException e) {
                System.err.println("Error saving JSON: " + e.getMessage());
            }
        }
    }
    
    /**
     * Checks which of the top 20 substances exist in Neo4j.
     * Returns a map of substance name to SubstanceInfo.
     */
    private Map<String, SubstanceInfo> checkSubstancesInNeo4j() {
        System.out.println("Step 1: Checking which substances exist in Neo4j...");
        
        Map<String, SubstanceInfo> result = new LinkedHashMap<>();
        
        for (String substanceName : TOP_20_SUBSTANCES) {
            String query = """
                MATCH (s:Substance)
                WHERE toLower(replace(s.name, '-', ' ')) CONTAINS toLower(replace($name, '-', ' '))
                OPTIONAL MATCH (rx:RXCUI)-[:REFERENCES]->(s)
                RETURN s.mmiId, s.name, collect(DISTINCT rx.code) AS rxcuis
                LIMIT 1
                """;
            
            Result queryResult = session.run(new Query(query, Map.of("name", substanceName)));
            
            if (queryResult.hasNext()) {
                var record = queryResult.next();
                Long mmiId = record.get("s.mmiId").asLong();
                String name = record.get("s.name").asString();
                List<String> rxcuis = record.get("rxcuis").asList(v -> v.asString());
                
                result.put(substanceName, new SubstanceInfo(mmiId, name, rxcuis));
                System.out.println("  ✓ Found: " + name + " (mmiId: " + mmiId + ", RxCUIs: " + rxcuis.size() + ")");
            } else {
                result.put(substanceName, new SubstanceInfo(null, substanceName, Collections.emptyList()));
                System.out.println("  ✗ Not found: " + substanceName);
            }
        }
        
        long foundCount = result.values().stream().filter(si -> si.mmiId != null).count();
        System.out.println("\n  Summary: " + foundCount + " / " + TOP_20_SUBSTANCES.size() + " substances found in Neo4j\n");
        
        return result;
    }
    
    /**
     * Finds all drugs that contain the given substances.
     * Returns a map of substance name to list of DrugInfo.
     * 
     * This method searches for all substances whose name contains the base substance name,
     * including variants like "Pantoprazol natrium-1,5-Wasser" when searching for "Pantoprazol".
     */
    private Map<String, List<DrugInfo>> findDrugsForSubstances(Map<String, SubstanceInfo> substanceInfo) {
        System.out.println("Step 2: Finding drugs containing these substances...");
        
        Map<String, List<DrugInfo>> result = new LinkedHashMap<>();
        
        for (Map.Entry<String, SubstanceInfo> entry : substanceInfo.entrySet()) {
            String substanceName = entry.getKey();
            SubstanceInfo info = entry.getValue();
            
            if (info.mmiId == null) {
                result.put(substanceName, Collections.emptyList());
                continue;
            }
            
            String query = """
                MATCH (p:Product)-[:CONTAINS]->(d:Drug)
                MATCH (d)-[:CONTAINS]->(ci:Ingredient)-[:IS_SUBSTANCE]->(s:Substance)
                WHERE toLower(replace(s.name, '-', ' ')) CONTAINS toLower(replace($substanceName, '-', ' '))
                OPTIONAL MATCH (ci)<-[:CORRESPONDS_TO]-(i:MmiIngredient)
                WITH p, d, CASE WHEN i IS NULL THEN ci ELSE i END AS ingredient
                WHERE ingredient.isActive = true
                RETURN DISTINCT p.mmiId AS productId, p.name AS productName, 
                       d.mmiId AS drugId
                """;
            
            Result queryResult = session.run(new Query(query, Map.of("substanceName", substanceName)));
            
            List<DrugInfo> drugs = new ArrayList<>();
            while (queryResult.hasNext()) {
                var record = queryResult.next();
                Long productId = record.get("productId").asLong();
                String productName = record.get("productName").asString();
                Long drugId = record.get("drugId").asLong();
                
                // Get full drug information for matching
                GraphDrug drug = loadDrugFromNeo4j(drugId);
                if (drug != null) {
                    drugs.add(new DrugInfo(productId, productName, drugId, drug));
                }
            }
            
            result.put(substanceName, drugs);
            System.out.println("  " + substanceName + ": " + drugs.size() + " drug(s) found");
        }
        
        long totalDrugs = result.values().stream().mapToLong(List::size).sum();
        System.out.println("\n  Summary: " + totalDrugs + " total drugs found\n");
        
        return result;
    }
    
    /**
     * Loads a complete GraphDrug from Neo4j by drug ID.
     * Uses the same query structure as Neo4jProductExporter.
     */
    private GraphDrug loadDrugFromNeo4j(Long drugId) {
        String query = 
            "MATCH (d:" + DRUG_LABEL + " {mmiId: $drugId}) " +
            "CALL {" +
            "    WITH d" +
            "    OPTIONAL MATCH (d)-[:" + DRUG_HAS_DOSE_FORM_LABEL + "]->(df:" + MMI_DOSE_FORM_LABEL + ") " +
            "    OPTIONAL MATCH (df)-[:" + DOSE_FORM_IS_EDQM + "]->(de:" + EDQM_LABEL + ")-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(dfcs:" + CODING_SYSTEM_LABEL + ") " +
            "    RETURN df, " + GraphUtil.groupCodingSystem("de", "dfcs",
            GraphEdqmPharmaceuticalDoseForm.NAME + ":de.name") + " AS edqmDoseForm" +
            "}" +
            "CALL {" +
            "    WITH d" +
            "    OPTIONAL MATCH (d)-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i:" + MMI_INGREDIENT_LABEL + ")-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(s:" + SUBSTANCE_LABEL + ") " +
            "    OPTIONAL MATCH (rx:" + RXCUI_LABEL + ")-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s) " +
            "    OPTIONAL MATCH (i)-[:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(ci:" + INGREDIENT_LABEL + ")-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(cis:" + SUBSTANCE_LABEL + ") " +
            "    OPTIONAL MATCH (ci)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(ciu:" + UNIT_LABEL + ") " +
            "    OPTIONAL MATCH (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(iu:" + UNIT_LABEL + ") " +
            "    WITH i, s, iu, collect(DISTINCT rx.code) AS rxCodes, " +
            "    collect(CASE WHEN ci IS NOT NULL THEN {" +
            SimpleGraphIngredient.SUBSTANCE_MMI_ID + ":cis.mmiId," +
            SimpleGraphIngredient.SUBSTANCE_NAME + ":cis.name," +
            SimpleGraphIngredient.MASS_FROM + ":ci.massFrom," +
            SimpleGraphIngredient.MASS_TO + ":ci.massTo," +
            SimpleGraphIngredient.UNIT + ":iu" +
            "} ELSE null END) AS corresponding " +
            "    RETURN collect(CASE WHEN s IS NOT NULL THEN {" +
            SimpleGraphIngredient.SUBSTANCE_MMI_ID + ":s.mmiId," +
            SimpleGraphIngredient.SUBSTANCE_NAME + ":s.name," +
            GraphIngredient.IS_ACTIVE + ":i.isActive," +
            SimpleGraphIngredient.MASS_FROM + ":i.massFrom," +
            SimpleGraphIngredient.MASS_TO + ":i.massTo," +
            SimpleGraphIngredient.UNIT + ":iu," +
            SimpleGraphIngredient.RXCUI_CODES + ":rxCodes," +
            GraphIngredient.CORRESPONDING_INGREDIENTS + ":corresponding" +
            "} ELSE null END) AS ingredients" +
            "}" +
            "OPTIONAL MATCH (d)-[:" + DRUG_HAS_UNIT_LABEL + "]->(du:" + UNIT_LABEL + ") " +
            "WITH d, df, edqmDoseForm, ingredients, du " +
            "RETURN {" +
            GraphDrug.INGREDIENTS + ":ingredients," +
            GraphDrug.ATC_CODES + ":[]," +
            GraphDrug.MMI_DOSE_FORM + ":df.mmiName," +
            GraphDrug.EDQM_DOSE_FORM + ":edqmDoseForm," +
            GraphDrug.AMOUNT + ":d.amount," +
            GraphDrug.UNIT + ":du" +
            "} AS drug";
        
        Result result = session.run(new Query(query, Map.of("drugId", drugId)));
        
        if (result.hasNext()) {
            var record = result.next();
            var drugValue = record.get("drug");
            return new GraphDrug(drugValue);
        }
        
        return null;
    }
    
    /**
     * Checks SCD matches for all drugs and calculates coverage.
     */
    private Map<String, CoverageResult> checkSCDMatches(Map<String, List<DrugInfo>> drugsBySubstance) {
        System.out.println("Step 3: Checking SCD matches for drugs...");
        
        Map<String, CoverageResult> results = new LinkedHashMap<>();
        
        for (Map.Entry<String, List<DrugInfo>> entry : drugsBySubstance.entrySet()) {
            String substanceName = entry.getKey();
            List<DrugInfo> drugs = entry.getValue();
            
            if (drugs.isEmpty()) {
                results.put(substanceName, new CoverageResult(0, 0, 0.0));
                continue;
            }
            
            int totalDrugs = drugs.size();
            int matchedDrugs = 0;
            
            for (DrugInfo drugInfo : drugs) {
                RxNormProductMatcher.MatchResult match = matcher.matchSCD(drugInfo.drug);
                if (match != null) {
                    matchedDrugs++;
                }
            }
            
            double percentage = (double) matchedDrugs / totalDrugs * 100.0;
            results.put(substanceName, new CoverageResult(totalDrugs, matchedDrugs, percentage));
            
            System.out.println("  " + substanceName + ": " + matchedDrugs + " / " + totalDrugs + 
                             " drugs matched (" + String.format("%.1f", percentage) + "%)");
        }
        
        System.out.println();
        return results;
    }
    
    /**
     * Prints the final analysis results.
     */
    private void printResults(Map<String, SubstanceInfo> substanceInfo, 
                              Map<String, CoverageResult> coverageResults) {
        System.out.println("=== Final Results ===\n");
        
        System.out.println("Substance Coverage Analysis:");
        System.out.println("─────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-25s | %-10s | %-10s | %-10s | %-10s%n", 
                         "Substance", "In Neo4j", "Total Drugs", "Matched", "Coverage");
        System.out.println("─────────────────────────────────────────────────────────────────────────");
        
        int totalSubstances = 0;
        int substancesInNeo4j = 0;
        int totalDrugs = 0;
        int totalMatched = 0;
        
        for (String substanceName : TOP_20_SUBSTANCES) {
            SubstanceInfo info = substanceInfo.get(substanceName);
            CoverageResult result = coverageResults.get(substanceName);
            
            String inNeo4j = info.mmiId != null ? "✓" : "✗";
            String totalDrugsStr = result.totalDrugs > 0 ? String.valueOf(result.totalDrugs) : "-";
            String matchedStr = result.totalDrugs > 0 ? String.valueOf(result.matchedDrugs) : "-";
            String coverageStr = result.totalDrugs > 0 ? String.format("%.1f%%", result.percentage) : "-";
            
            System.out.printf("%-25s | %-10s | %-10s | %-10s | %-10s%n",
                             substanceName, inNeo4j, totalDrugsStr, matchedStr, coverageStr);
            
            totalSubstances++;
            if (info.mmiId != null) {
                substancesInNeo4j++;
            }
            totalDrugs += result.totalDrugs;
            totalMatched += result.matchedDrugs;
        }
        
        System.out.println("─────────────────────────────────────────────────────────────────────────");
        
        double overallCoverage = totalDrugs > 0 ? (double) totalMatched / totalDrugs * 100.0 : 0.0;
        
        System.out.println("\nSummary:");
        System.out.println("  Substances in Neo4j: " + substancesInNeo4j + " / " + totalSubstances);
        System.out.println("  Total drugs analyzed: " + totalDrugs);
        System.out.println("  Drugs with SCD match: " + totalMatched);
        System.out.println("  Overall coverage: " + String.format("%.1f%%", overallCoverage));
    }
    
    /**
     * Saves the analysis results to a JSON file.
     */
    private void saveResultsToJson(Map<String, SubstanceInfo> substanceInfo,
                                   Map<String, CoverageResult> coverageResults,
                                   String filename) throws IOException {
        // Ensure directory exists
        Path filePath = Paths.get(filename);
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        Map<String, Object> jsonData = new LinkedHashMap<>();
        
        // Calculate summary statistics
        int totalSubstances = 0;
        int substancesInNeo4j = 0;
        int totalDrugs = 0;
        int totalMatched = 0;
        
        List<Map<String, Object>> substances = new ArrayList<>();
        for (String substanceName : TOP_20_SUBSTANCES) {
            SubstanceInfo info = substanceInfo.get(substanceName);
            CoverageResult result = coverageResults.get(substanceName);
            
            Map<String, Object> substanceData = new LinkedHashMap<>();
            substanceData.put("name", substanceName);
            substanceData.put("foundInNeo4j", info.mmiId != null);
            substanceData.put("neo4jName", info.name);
            substanceData.put("rxcuis", info.rxcuis);
            substanceData.put("totalDrugs", result.totalDrugs);
            substanceData.put("matchedDrugs", result.matchedDrugs);
            substanceData.put("coverage", result.percentage);
            
            substances.add(substanceData);
            
            totalSubstances++;
            if (info.mmiId != null) {
                substancesInNeo4j++;
            }
            totalDrugs += result.totalDrugs;
            totalMatched += result.matchedDrugs;
        }
        
        double overallCoverage = totalDrugs > 0 ? (double) totalMatched / totalDrugs * 100.0 : 0.0;
        
        jsonData.put("substances", substances);
        jsonData.put("summary", Map.of(
            "totalSubstances", totalSubstances,
            "substancesInNeo4j", substancesInNeo4j,
            "totalDrugs", totalDrugs,
            "totalMatched", totalMatched,
            "overallCoverage", overallCoverage
        ));
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(jsonData, writer);
        }
    }
    
    // Data classes
    
    private record SubstanceInfo(Long mmiId, String name, List<String> rxcuis) {}
    
    private record DrugInfo(Long productId, String productName, Long drugId, GraphDrug drug) {}
    
    private record CoverageResult(int totalDrugs, int matchedDrugs, double percentage) {}
}

