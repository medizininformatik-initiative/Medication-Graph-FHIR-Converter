package de.medizininformatikinitiative.medgraph.tools;

import org.neo4j.driver.*;
import java.util.*;

/**
 * Detaillierte Analyse der Probleme bei Ibuprofen-Matching
 */
public class AnalyzeIbuprofenProblems {
    public static void main(String[] args) {
        String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost:7687");
        String user = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        String password = System.getenv("NEO4J_PASSWORD");
        if (password == null || password.isEmpty()) {
            System.err.println("WARNING: No Neo4j password provided!");
            System.err.println("Please set NEO4J_PASSWORD environment variable.");
            System.exit(1);
        }
        
        System.out.println("=== Ibuprofen Coverage Problem-Analyse ===\n");
        System.out.println("Connecting to Neo4j: " + uri + " as " + user);
        AuthToken token = AuthTokens.basic(user, password);
        
        try (Driver driver = GraphDatabase.driver(uri, token, Config.defaultConfig());
             Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            
            String substanceName = "Ibuprofen";
            
            System.out.println("\n=== Schritt 1: Substanz-Info ===");
            String query1 = """
                MATCH (s:Substance)
                WHERE toLower(replace(s.name, '-', ' ')) CONTAINS toLower(replace($name, '-', ' '))
                OPTIONAL MATCH (rx:RXCUI)-[:REFERENCES]->(s)
                RETURN s.mmiId, s.name, collect(DISTINCT rx.code) AS rxcuis
                LIMIT 1
                """;
            
            Result result1 = session.run(query1, Map.of("name", substanceName));
            Long mmiId = null;
            String foundName = null;
            List<String> rxcuis = Collections.emptyList();
            
            if (result1.hasNext()) {
                var record = result1.next();
                mmiId = record.get("s.mmiId").asLong();
                foundName = record.get("s.name").asString();
                rxcuis = record.get("rxcuis").asList(v -> v.asString());
                System.out.println("  Substanz: " + foundName + " (mmiId: " + mmiId + ")");
                System.out.println("  RxCUIs: " + rxcuis);
            }
            
            System.out.println("\n=== Schritt 2: Analysiere Dose Forms ohne Matches ===");
            String query2 = """
                MATCH (s:Substance {mmiId: $mmiId})
                MATCH (s)<-[:IS_SUBSTANCE]-(ci:Ingredient)
                OPTIONAL MATCH (ci)<-[:CORRESPONDS_TO]-(i:MmiIngredient)
                WITH s, CASE WHEN i IS NULL THEN ci ELSE i END AS ingredient 
                WHERE ingredient.isActive = true
                MATCH (ingredient)<-[:CONTAINS]-(d:Drug)<-[:CONTAINS]-(p:Product)
                OPTIONAL MATCH (d)-[:HAS_DOSE_FORM]->(df:MmiDoseForm)
                OPTIONAL MATCH (df)-[:IS_EDQM]->(edqm:EDQM)
                RETURN DISTINCT df.mmiName AS mmiDoseForm,
                       edqm.name AS edqmDoseForm,
                       count(DISTINCT d) AS drugCount
                ORDER BY drugCount DESC
                """;
            
            Result result2 = session.run(query2, Map.of("mmiId", mmiId));
            Map<String, Integer> doseFormCounts = new LinkedHashMap<>();
            while (result2.hasNext()) {
                var record = result2.next();
                String mmiDF = record.get("mmiDoseForm").asString(null);
                String edqmDF = record.get("edqmDoseForm").asString(null);
                int count = record.get("drugCount").asInt();
                String key = mmiDF + (edqmDF != null ? " (EDQM: " + edqmDF + ")" : " (kein EDQM)");
                doseFormCounts.put(key, count);
            }
            
            System.out.println("  Dose Forms mit Ibuprofen:");
            for (Map.Entry<String, Integer> entry : doseFormCounts.entrySet()) {
                System.out.println("    - " + entry.getKey() + ": " + entry.getValue() + " Drugs");
            }
            
            System.out.println("\n=== Schritt 3: Analysiere verschiedene Stärken ===");
            String query3 = """
                MATCH (s:Substance {mmiId: $mmiId})
                MATCH (s)<-[:IS_SUBSTANCE]-(ci:Ingredient)
                OPTIONAL MATCH (ci)<-[:CORRESPONDS_TO]-(i:MmiIngredient)
                WITH s, CASE WHEN i IS NULL THEN ci ELSE i END AS ingredient 
                WHERE ingredient.isActive = true
                MATCH (ingredient)<-[:CONTAINS]-(d:Drug)<-[:CONTAINS]-(p:Product)
                OPTIONAL MATCH (ingredient)-[:HAS_UNIT]->(u:Unit)
                WITH ingredient.massFrom + "-" + ingredient.massTo + " " + COALESCE(u.name, "") AS strengthPattern,
                     count(DISTINCT d) AS drugCount
                RETURN strengthPattern, drugCount
                ORDER BY drugCount DESC
                LIMIT 20
                """;
            
            Result result3 = session.run(query3, Map.of("mmiId", mmiId));
            System.out.println("  Top Stärken:");
            while (result3.hasNext()) {
                var record = result3.next();
                String strength = record.get("strengthPattern").asString();
                int count = record.get("drugCount").asInt();
                System.out.println("    - " + strength + ": " + count + " Drugs");
            }
            
            System.out.println("\n=== Schritt 4: Analysiere Kombinationspräparate ===");
            String query4 = """
                MATCH (s:Substance {mmiId: $mmiId})
                MATCH (s)<-[:IS_SUBSTANCE]-(ci:Ingredient)
                OPTIONAL MATCH (ci)<-[:CORRESPONDS_TO]-(i:MmiIngredient)
                WITH s, CASE WHEN i IS NULL THEN ci ELSE i END AS ingredient 
                WHERE ingredient.isActive = true
                MATCH (ingredient)<-[:CONTAINS]-(d:Drug)<-[:CONTAINS]-(p:Product)
                MATCH (d)-[:CONTAINS]->(otherIng:MmiIngredient {isActive: true})-[:IS_SUBSTANCE]->(otherSub:Substance)
                WHERE otherSub.mmiId <> $mmiId
                WITH d, p, collect(DISTINCT otherSub.name) AS otherSubstances
                WHERE size(otherSubstances) > 0
                RETURN DISTINCT p.name AS productName,
                       d.mmiId AS drugId,
                       otherSubstances
                LIMIT 10
                """;
            
            Result result4 = session.run(query4, Map.of("mmiId", mmiId));
            System.out.println("  Beispiel-Kombinationspräparate:");
            int comboCount = 0;
            while (result4.hasNext()) {
                var record = result4.next();
                comboCount++;
                String productName = record.get("productName").asString();
                List<String> otherSubs = record.get("otherSubstances").asList(v -> v.asString());
                System.out.println("    " + comboCount + ". " + productName);
                System.out.println("       Zusätzliche Substanzen: " + String.join(", ", otherSubs));
            }
            
            System.out.println("\n=== Schritt 5: Analysiere Ibuprofen-Salze (z.B. Ibuprofen lysin) ===");
            String query5 = """
                MATCH (s:Substance)
                WHERE toLower(s.name) CONTAINS 'ibuprofen' AND s.mmiId <> $mmiId
                OPTIONAL MATCH (s)<-[:IS_SUBSTANCE]-(ci:Ingredient)
                OPTIONAL MATCH (ci)<-[:CORRESPONDS_TO]-(i:MmiIngredient)
                WITH s, CASE WHEN i IS NULL THEN ci ELSE i END AS ingredient 
                WHERE ingredient.isActive = true
                MATCH (ingredient)<-[:CONTAINS]-(d:Drug)<-[:CONTAINS]-(p:Product)
                RETURN DISTINCT s.name AS substanceName,
                       s.mmiId AS substanceMmiId,
                       count(DISTINCT d) AS drugCount
                ORDER BY drugCount DESC
                LIMIT 10
                """;
            
            Result result5 = session.run(query5, Map.of("mmiId", mmiId));
            System.out.println("  Ibuprofen-Varianten (Salze, etc.):");
            int variantCount = 0;
            while (result5.hasNext()) {
                var record = result5.next();
                variantCount++;
                String subName = record.get("substanceName").asString();
                Long subMmiId = record.get("substanceMmiId").asLong();
                int count = record.get("drugCount").asInt();
                System.out.println("    " + variantCount + ". " + subName + " (mmiId: " + subMmiId + "): " + count + " Drugs");
            }
            
            System.out.println("\n=== Zusammenfassung der Probleme ===");
            System.out.println("Basierend auf den Logs identifizierte Probleme:");
            System.out.println("\n1. STRENGTH-MISMATCHES:");
            System.out.println("   - Viele Dose Forms haben Kandidaten, aber keine passenden Stärken");
            System.out.println("   - Betroffen: Infusionslsg., Susp. zum Einnehmen, Gel, Zäpfchen, Injektionslsg.");
            System.out.println("   - Grund: RxNorm hat andere Stärken als die deutschen Products");
            
            System.out.println("\n2. KOMBINATIONSPRÄPARATE:");
            System.out.println("   - Drugs mit Ibuprofen + anderen Ingredients (z.B. Coffein, Pseudoephedrin)");
            System.out.println("   - Problem: RxNorm hat möglicherweise andere Kombinationen oder Stärken");
            
            System.out.println("\n3. IBUPROFEN-SALZE:");
            System.out.println("   - 'Ibuprofen lysin' ist eine andere Substanz als 'Ibuprofen'");
            System.out.println("   - Wird nicht über CORRESPONDS_TO verknüpft");
            System.out.println("   - Daher werden diese Drugs nicht gefunden");
            
            System.out.println("\n4. PROBLEMATISCHE MATCHES:");
            System.out.println("   - Drugs mit nur Ibuprofen matchen auf Kombinationspräparate");
            System.out.println("   - Beispiel: 'Ibuprofen 400 mg' matcht auf 'ibuprofen 400 MG / oxycodone 5 MG'");
            System.out.println("   - Das ist ein Bug in der Ingredients-Kompatibilitätsprüfung! (gefixed)");
            
        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

