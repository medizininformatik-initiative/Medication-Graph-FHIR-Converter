package de.medizininformatikinitiative.medgraph.tools;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphDrug;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.LocalRxNormCandidateProvider;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.RxNormProductMatcher;
import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Detaillierte Analyse zu Acetylsalicylsäure 0% Coverage
 */
public class AnalyzeAcetylsalicylsaeure {
    
    public static void main(String[] args) {
        String uri = args.length > 0 ? args[0] : "bolt://localhost:7687";
        String user = args.length > 1 ? args[1] : "neo4j";
        String password = args.length > 2 ? args[2] : System.getenv().getOrDefault("NEO4J_PASSWORD", "");
        if (password.isEmpty()) {
            System.err.println("WARNING: No Neo4j password provided!");
            System.err.println("Please set NEO4J_PASSWORD environment variable or pass as command line argument.");
            System.exit(1);
        }
        
        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
             Session session = driver.session()) {
            
            System.out.println("=== Acetylsalicylsäure Coverage Problem-Analyse ===\n");
            
            // Step 1: Finde Substanz
            System.out.println("Step 1: Finde Acetylsalicylsäure in Neo4j...");
            Long substanceMmiId = findSubstance(session, "Acetylsalicylsäure");
            if (substanceMmiId == null) {
                System.out.println("  ✗ Acetylsalicylsäure nicht gefunden!");
                return;
            }
            System.out.println("  ✓ Gefunden mit mmiId: " + substanceMmiId);
            
            // Step 2: Prüfe RxCUIs
            System.out.println("\nStep 2: Prüfe RxCUIs...");
            List<String> rxcuis = getRxCUIs(session, substanceMmiId);
            System.out.println("  RxCUIs: " + rxcuis);
            if (rxcuis.isEmpty()) {
                System.out.println("  ⚠ WARNING: Keine RxCUIs gefunden!");
                System.out.println("  → Problem: Matcher kann keine SCD-Kandidaten finden ohne RxCUIs");
            }
            
            // Step 3: Zähle Drugs
            System.out.println("\nStep 3: Zähle Drugs...");
            int totalDrugs = countDrugs(session, substanceMmiId);
            System.out.println("  Total Drugs: " + totalDrugs);
            
            // Step 4: Analysiere Dose Forms
            System.out.println("\nStep 4: Analysiere Dose Forms...");
            analyzeDoseForms(session, substanceMmiId);
            
            // Step 5: Analysiere Sample Drugs
            System.out.println("\nStep 5: Analysiere Sample Drugs (warum Matching scheitert)...");
            List<DrugSample> drugs = findDrugs(session, substanceMmiId, 20);
            
            if (drugs.isEmpty()) {
                System.out.println("  ✗ Keine Drugs gefunden!");
                return;
            }
            
            // Step 6: Teste Matching für Sample Drugs
            System.out.println("\nStep 6: Teste Matching für Sample Drugs...");
            LocalRxNormCandidateProvider provider = new LocalRxNormCandidateProvider("data/rxnorm/rxnorm.db");
            RxNormProductMatcher matcher = new RxNormProductMatcher();
            RxNormProductMatcher.setCandidateProvider(provider);
            
            int noIngredients = 0;
            int noRxCUIs = 0;
            int noDoseForm = 0;
            int noCandidates = 0;
            int noStrengthMatch = 0;
            int matched = 0;
            
            for (int i = 0; i < Math.min(20, drugs.size()); i++) {
                DrugSample drug = drugs.get(i);
                System.out.println("\n  Sample " + (i+1) + ": " + drug.productName);
                System.out.println("    Drug ID: " + drug.drugId);
                System.out.println("    Ingredients: " + drug.ingredientInfo);
                System.out.println("    Dose Form: " + drug.mmiDoseForm + " -> " + drug.edqmDoseForm);
                
                RxNormProductMatcher.MatchResult result = matcher.matchSCD(drug.drug);
                
                if (result == null) {
                    // Versuche zu bestimmen warum
                    if (drug.drug.ingredients().isEmpty()) {
                        noIngredients++;
                        System.out.println("    ✗ REJECTED: Keine Ingredients");
                    } else {
                        // Prüfe RxCUIs
                        boolean hasRxCUIs = false;
                        for (var ing : drug.drug.ingredients()) {
                            if (ing.isActive() && ing instanceof de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.SimpleGraphIngredient) {
                                var simpleIng = (de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.SimpleGraphIngredient) ing;
                                if (simpleIng.getRxcuiCodes() != null && !simpleIng.getRxcuiCodes().isEmpty()) {
                                    hasRxCUIs = true;
                                    break;
                                }
                            }
                        }
                        if (!hasRxCUIs) {
                            noRxCUIs++;
                            System.out.println("    ✗ REJECTED: Keine RxCUIs für Ingredients");
                        } else if (drug.edqmDoseForm == null || drug.edqmDoseForm.isEmpty()) {
                            noDoseForm++;
                            System.out.println("    ✗ REJECTED: Keine Dose Form Mapping");
                        } else {
                            // Weitere Analyse würde hier erfolgen
                            noCandidates++;
                            System.out.println("    ✗ REJECTED: Keine SCD-Kandidaten gefunden oder Strength-Mismatch");
                        }
                    }
                } else {
                    matched++;
                    System.out.println("    ✓ MATCHED: " + result.name + " (rxcui=" + result.rxcui + ")");
                }
            }
            
            // Summary
            System.out.println("\n=== Zusammenfassung ===");
            System.out.println("Total Drugs: " + totalDrugs);
            System.out.println("Sample analysiert: " + Math.min(20, drugs.size()));
            System.out.println("Matched in Sample: " + matched);
            System.out.println("\nRejection-Gründe (Sample):");
            System.out.println("  - Keine Ingredients: " + noIngredients);
            System.out.println("  - Keine RxCUIs: " + noRxCUIs);
            System.out.println("  - Keine Dose Form Mapping: " + noDoseForm);
            System.out.println("  - Keine SCD-Kandidaten/Strength-Mismatch: " + noCandidates);
            System.out.println("  - Keine Strength-Matches: " + noStrengthMatch);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static Long findSubstance(Session session, String name) {
        String query = """
            MATCH (s:Substance)
            WHERE toLower(replace(s.name, '-', ' ')) CONTAINS toLower(replace($name, '-', ' '))
            RETURN s.mmiId, s.name
            LIMIT 1
            """;
        
        Result result = session.run(query, Map.of("name", name));
        if (result.hasNext()) {
            var record = result.next();
            System.out.println("  Gefundene Substanz: " + record.get("s.name").asString());
            return record.get("s.mmiId").asLong();
        }
        return null;
    }
    
    private static List<String> getRxCUIs(Session session, Long substanceMmiId) {
        String query = """
            MATCH (s:Substance {mmiId: $mmiId})
            OPTIONAL MATCH (rx:RXCUI)-[:REFERENCES]->(s)
            RETURN collect(DISTINCT rx.code) AS rxcuis
            """;
        
        Result result = session.run(query, Map.of("mmiId", substanceMmiId));
        if (result.hasNext()) {
            return result.next().get("rxcuis").asList(v -> v.asString());
        }
        return List.of();
    }
    
    private static int countDrugs(Session session, Long substanceMmiId) {
        String query = """
            MATCH (s:Substance {mmiId: $mmiId})
            MATCH (s)<-[:IS_SUBSTANCE]-(i:MmiIngredient {isActive: true})
            MATCH (i)<-[:CONTAINS]-(d:Drug)
            RETURN count(DISTINCT d) AS total
            """;
        
        Result result = session.run(query, Map.of("mmiId", substanceMmiId));
        if (result.hasNext()) {
            return result.next().get("total").asInt();
        }
        return 0;
    }
    
    private static void analyzeDoseForms(Session session, Long substanceMmiId) {
        String query = """
            MATCH (s:Substance {mmiId: $mmiId})
            MATCH (s)<-[:IS_SUBSTANCE]-(i:MmiIngredient {isActive: true})
            MATCH (i)<-[:CONTAINS]-(d:Drug)
            OPTIONAL MATCH (d)-[:HAS_DOSE_FORM]->(df:MmiDoseForm)
            OPTIONAL MATCH (df)-[:IS_EDQM]->(edqm:EdqmPharmaceuticalDoseForm)
            OPTIONAL MATCH (edqm)-[:HAS_RXNORM_MAPPING]->(m:EdqmRxNormMapping)
            RETURN df.mmiName AS mmiDoseForm, 
                   edqm.name AS edqmDoseForm,
                   m.rxnormDoseForm AS rxnormDoseForm,
                   count(DISTINCT d) AS drugCount
            ORDER BY drugCount DESC
            LIMIT 15
            """;
        
        Result result = session.run(query, Map.of("mmiId", substanceMmiId));
        System.out.println("  Top Dose Forms:");
        int total = 0;
        int withMapping = 0;
        while (result.hasNext()) {
            var record = result.next();
            String mmiDF = record.get("mmiDoseForm").isNull() ? "null" : record.get("mmiDoseForm").asString();
            String edqmDF = record.get("edqmDoseForm").isNull() ? "null" : record.get("edqmDoseForm").asString();
            String rxnormDF = record.get("rxnormDoseForm").isNull() ? "null" : record.get("rxnormDoseForm").asString();
            int count = record.get("drugCount").asInt();
            total += count;
            if (!rxnormDF.equals("null")) withMapping += count;
            System.out.println("    - " + mmiDF + " -> " + edqmDF + " -> " + rxnormDF + " (" + count + " Drugs)");
        }
        System.out.println("  Total: " + total + " Drugs, " + withMapping + " mit RxNorm-Mapping");
    }
    
    private static List<DrugSample> findDrugs(Session session, Long substanceMmiId, int limit) {
        String query = """
            MATCH (s:Substance {mmiId: $substanceMmiId})
            MATCH (s)<-[:IS_SUBSTANCE]-(i:MmiIngredient {isActive: true})
            MATCH (i)<-[:CONTAINS]-(d:Drug)<-[:CONTAINS]-(p:Product)
            OPTIONAL MATCH (d)-[:HAS_DOSE_FORM]->(df:MmiDoseForm)
            OPTIONAL MATCH (df)-[:IS_EDQM]->(edqm:EdqmPharmaceuticalDoseForm)
            OPTIONAL MATCH (rx:RXCUI)-[:REFERENCES]->(s)
            RETURN DISTINCT p.name AS productName,
                   d.mmiId AS drugId,
                   i.massFrom AS strength,
                   df.mmiName AS mmiDoseForm,
                   edqm.name AS edqmDoseForm,
                   collect(DISTINCT rx.code) AS rxcuis
            LIMIT $limit
            """;
        
        Result result = session.run(query, Map.of("substanceMmiId", substanceMmiId, "limit", limit));
        List<DrugSample> drugs = new ArrayList<>();
        
        while (result.hasNext()) {
            var record = result.next();
            Long drugId = record.get("drugId").asLong();
            
            // Load full drug
            GraphDrug drug = loadDrug(session, drugId);
            if (drug != null) {
                String ingredientInfo = drug.ingredients().stream()
                    .filter(ing -> ing.isActive())
                    .map(ing -> ing.getSubstanceName() + " " + 
                         (ing.getMassFrom() != null ? ing.getMassFrom() : "") + 
                         (ing.getUnit() != null ? " " + ing.getUnit().print() : ""))
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("none");
                
                drugs.add(new DrugSample(
                    record.get("productName").asString(),
                    drugId,
                    record.get("mmiDoseForm").isNull() ? null : record.get("mmiDoseForm").asString(),
                    record.get("edqmDoseForm").isNull() ? null : record.get("edqmDoseForm").asString(),
                    ingredientInfo,
                    drug
                ));
            }
        }
        
        return drugs;
    }
    
    private static GraphDrug loadDrug(Session session, Long drugId) {
        String query = 
            "MATCH (d:" + DRUG_LABEL + " {mmiId: $drugId}) " +
            "CALL {" +
            "    WITH d" +
            "    OPTIONAL MATCH (d)-[:" + DRUG_HAS_DOSE_FORM_LABEL + "]->(df:" + MMI_DOSE_FORM_LABEL + ") " +
            "    OPTIONAL MATCH (df)-[:" + DOSE_FORM_IS_EDQM + "]->(de:" + EDQM_LABEL + ")-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(dfcs:" + CODING_SYSTEM_LABEL + ") " +
            "    RETURN df, " + de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphUtil.groupCodingSystem("de", "dfcs",
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphEdqmPharmaceuticalDoseForm.NAME + ":de.name") + " AS edqmDoseForm" +
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
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.SimpleGraphIngredient.SUBSTANCE_MMI_ID + ":cis.mmiId," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.SimpleGraphIngredient.SUBSTANCE_NAME + ":cis.name," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.SimpleGraphIngredient.MASS_FROM + ":ci.massFrom," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.SimpleGraphIngredient.MASS_TO + ":ci.massTo," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.SimpleGraphIngredient.UNIT + ":iu" +
            "} ELSE null END) AS corresponding " +
            "    RETURN collect(CASE WHEN s IS NOT NULL THEN {" +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.SimpleGraphIngredient.SUBSTANCE_MMI_ID + ":s.mmiId," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.SimpleGraphIngredient.SUBSTANCE_NAME + ":s.name," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphIngredient.IS_ACTIVE + ":i.isActive," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.SimpleGraphIngredient.MASS_FROM + ":i.massFrom," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.SimpleGraphIngredient.MASS_TO + ":i.massTo," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.SimpleGraphIngredient.UNIT + ":iu," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.SimpleGraphIngredient.RXCUI_CODES + ":rxCodes," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphIngredient.CORRESPONDING_INGREDIENTS + ":corresponding" +
            "} ELSE null END) AS ingredients" +
            "}" +
            "OPTIONAL MATCH (d)-[:" + DRUG_HAS_UNIT_LABEL + "]->(du:" + UNIT_LABEL + ") " +
            "WITH d, df, edqmDoseForm, ingredients, du " +
            "RETURN {" +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphDrug.INGREDIENTS + ":ingredients," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphDrug.ATC_CODES + ":[]," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphDrug.MMI_DOSE_FORM + ":df.mmiName," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphDrug.EDQM_DOSE_FORM + ":edqmDoseForm," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphDrug.AMOUNT + ":d.amount," +
            de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphDrug.UNIT + ":du" +
            "} AS drug";
        
        Result result = session.run(query, Map.of("drugId", drugId));
        
        if (result.hasNext()) {
            var record = result.next();
            var drugValue = record.get("drug");
            return new GraphDrug(drugValue);
        }
        
        return null;
    }
    
    private record DrugSample(String productName, Long drugId, String mmiDoseForm, 
                              String edqmDoseForm, String ingredientInfo, GraphDrug drug) {}
}

