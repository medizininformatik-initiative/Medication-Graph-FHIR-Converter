package de.medizininformatikinitiative.medgraph.tools;

import org.neo4j.driver.*;
import java.util.*;

/**
 * Analyse-Tool für Ibuprofen - identifiziert Probleme bei der SCD-Matching-Coverage
 */
public class AnalyzeIbuprofen {
    public static void main(String[] args) {
        String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost:7687");
        String user = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        String password = System.getenv("NEO4J_PASSWORD");
        if (password == null || password.isEmpty()) {
            System.err.println("WARNING: No Neo4j password provided!");
            System.err.println("Please set NEO4J_PASSWORD environment variable.");
            System.exit(1);
        }
        
        System.out.println("Connecting to Neo4j: " + uri + " as " + user);
        AuthToken token = AuthTokens.basic(user, password);
        
        try (Driver driver = GraphDatabase.driver(uri, token, Config.defaultConfig());
             Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            
            String substanceName = "Ibuprofen";
            
            System.out.println("\n=== Schritt 1: Prüfe ob Ibuprofen in Neo4j existiert ===");
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
                System.out.println("  ✓ Gefunden: " + foundName + " (mmiId: " + mmiId + ", RxCUIs: " + rxcuis.size() + ")");
            } else {
                System.out.println("  ✗ Nicht gefunden: " + substanceName);
                return;
            }
            
            System.out.println("\n=== Schritt 2: Analysiere verschiedene Ingredients und ihre Stärken ===");
            String query2 = """
                MATCH (s:Substance {mmiId: $mmiId})
                MATCH (s)<-[:IS_SUBSTANCE]-(ci:Ingredient)
                OPTIONAL MATCH (ci)<-[:CORRESPONDS_TO]-(i:MmiIngredient)
                WITH s, CASE WHEN i IS NULL THEN ci ELSE i END AS ingredient 
                WHERE ingredient.isActive = true
                MATCH (ingredient)<-[:CONTAINS]-(d:Drug)<-[:CONTAINS]-(p:Product)
                OPTIONAL MATCH (ingredient)-[:HAS_UNIT]->(u:Unit)
                RETURN DISTINCT ingredient.mmiId AS ingredientId, 
                       ingredient.massFrom AS massFrom,
                       ingredient.massTo AS massTo,
                       u.name AS unitName,
                       count(DISTINCT d) AS drugCount
                ORDER BY drugCount DESC
                LIMIT 20
                """;
            
            Result result2 = session.run(query2, Map.of("mmiId", mmiId));
            System.out.println("  Top Ingredients nach Häufigkeit:");
            int ingCount = 0;
            Map<String, Integer> strengthPatterns = new LinkedHashMap<>();
            while (result2.hasNext()) {
                var record = result2.next();
                ingCount++;
                String massFrom = record.get("massFrom").asString(null);
                String massTo = record.get("massTo").asString(null);
                String unit = record.get("unitName").asString(null);
                int drugCount = record.get("drugCount").asInt();
                String strengthPattern = massFrom + "-" + massTo + " " + unit;
                strengthPatterns.put(strengthPattern, drugCount);
                System.out.println("    " + ingCount + ". Stärke: " + strengthPattern + " → " + drugCount + " Drugs");
            }
            
            System.out.println("\n=== Schritt 3: Analysiere Dose Forms der Drugs ===");
            String query3 = """
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
                LIMIT 15
                """;
            
            Result result3 = session.run(query3, Map.of("mmiId", mmiId));
            System.out.println("  Top Dose Forms:");
            int dfCount = 0;
            while (result3.hasNext()) {
                var record = result3.next();
                dfCount++;
                String mmiDF = record.get("mmiDoseForm").asString(null);
                String edqmDF = record.get("edqmDoseForm").asString(null);
                int drugCount = record.get("drugCount").asInt();
                System.out.println("    " + dfCount + ". " + mmiDF + 
                                 (edqmDF != null ? " (EDQM: " + edqmDF + ")" : " (kein EDQM)") + 
                                 " → " + drugCount + " Drugs");
            }
            
            System.out.println("\n=== Schritt 4: Beispiel-Products mit verschiedenen Kombinationen ===");
            String query4 = """
                MATCH (s:Substance {mmiId: $mmiId})
                MATCH (s)<-[:IS_SUBSTANCE]-(ci:Ingredient)
                OPTIONAL MATCH (ci)<-[:CORRESPONDS_TO]-(i:MmiIngredient)
                WITH s, CASE WHEN i IS NULL THEN ci ELSE i END AS ingredient 
                WHERE ingredient.isActive = true
                MATCH (ingredient)<-[:CONTAINS]-(d:Drug)<-[:CONTAINS]-(p:Product)
                OPTIONAL MATCH (d)-[:HAS_DOSE_FORM]->(df:MmiDoseForm)
                OPTIONAL MATCH (ingredient)-[:HAS_UNIT]->(u:Unit)
                OPTIONAL MATCH (df)-[:IS_EDQM]->(edqm:EDQM)
                WITH p, d, ingredient, df, u, edqm,
                     ingredient.massFrom + " " + COALESCE(u.name, "") AS strengthStr
                RETURN DISTINCT p.name AS productName,
                       strengthStr AS strength,
                       df.mmiName AS doseForm,
                       edqm.name AS edqmDoseForm,
                       count(DISTINCT d) OVER (PARTITION BY strengthStr, df.mmiName) AS count
                ORDER BY count DESC
                LIMIT 10
                """;
            
            Result result4 = session.run(query4, Map.of("mmiId", mmiId));
            System.out.println("  Beispiel-Products:");
            int prodCount = 0;
            while (result4.hasNext()) {
                var record = result4.next();
                prodCount++;
                String productName = record.get("productName").asString();
                String strength = record.get("strength").asString();
                String doseForm = record.get("doseForm").asString(null);
                String edqmDF = record.get("edqmDoseForm").asString(null);
                System.out.println("    " + prodCount + ". " + productName);
                System.out.println("       Stärke: " + strength);
                System.out.println("       Dose Form: " + doseForm + 
                                 (edqmDF != null ? " (EDQM: " + edqmDF + ")" : " (kein EDQM)"));
            }
            
            System.out.println("\n=== Schritt 5: Prüfe Ingredients in einem Beispiel-Drug (mit allen Details) ===");
            String query5 = """
                MATCH (s:Substance {mmiId: $mmiId})
                MATCH (s)<-[:IS_SUBSTANCE]-(ci:Ingredient)
                OPTIONAL MATCH (ci)<-[:CORRESPONDS_TO]-(i:MmiIngredient)
                WITH s, CASE WHEN i IS NULL THEN ci ELSE i END AS ingredient 
                WHERE ingredient.isActive = true
                MATCH (ingredient)<-[:CONTAINS]-(d:Drug)<-[:CONTAINS]-(p:Product)
                RETURN DISTINCT p.mmiId AS productId, p.name AS productName, d.mmiId AS drugId
                LIMIT 1
                """;
            
            Result result5 = session.run(query5, Map.of("mmiId", mmiId));
            if (result5.hasNext()) {
                var record = result5.next();
                Long exampleDrugId = record.get("drugId").asLong();
                String exampleProductName = record.get("productName").asString();
                
                System.out.println("  Analysiere Drug ID " + exampleDrugId + " aus Product: " + exampleProductName);
                
                String query6 = """
                    MATCH (d:Drug {mmiId: $drugId})
                    OPTIONAL MATCH (d)-[:HAS_DOSE_FORM]->(df:MmiDoseForm)
                    OPTIONAL MATCH (df)-[:IS_EDQM]->(edqm:EDQM)
                    OPTIONAL MATCH (d)-[:CONTAINS]->(i:MmiIngredient)-[:IS_SUBSTANCE]->(s:Substance)
                    OPTIONAL MATCH (i)-[:CORRESPONDS_TO]->(ci:Ingredient)-[:IS_SUBSTANCE]->(cs:Substance)
                    OPTIONAL MATCH (i)-[:HAS_UNIT]->(u:Unit)
                    OPTIONAL MATCH (rx:RXCUI)-[:REFERENCES]->(s)
                    RETURN d.mmiId AS drugId,
                           df.mmiName AS mmiDoseForm,
                           edqm.name AS edqmDoseForm,
                           i.mmiId AS ingredientId,
                           i.isActive AS isActive,
                           i.massFrom AS massFrom,
                           i.massTo AS massTo,
                           u.name AS unitName,
                           s.name AS substanceName,
                           s.mmiId AS substanceMmiId,
                           collect(DISTINCT rx.code) AS rxcuis,
                           ci.massFrom AS corrMassFrom,
                           cs.name AS corrSubstanceName
                    ORDER BY i.isActive DESC
                    """;
                
                Result result6 = session.run(query6, Map.of("drugId", exampleDrugId));
                System.out.println("    Drug Details:");
                boolean first = true;
                while (result6.hasNext()) {
                    var rec = result6.next();
                    if (first) {
                        System.out.println("      - Dose Form (MMI): " + rec.get("mmiDoseForm").asString(null));
                        System.out.println("      - Dose Form (EDQM): " + rec.get("edqmDoseForm").asString(null));
                        first = false;
                    }
                    System.out.println("      Ingredient:");
                    System.out.println("        - Substanz: " + rec.get("substanceName").asString() + 
                                     " (mmiId: " + rec.get("substanceMmiId").asLong() + ")");
                    System.out.println("        - RxCUIs: " + rec.get("rxcuis").asList());
                    System.out.println("        - Stärke: " + rec.get("massFrom").asString(null) + 
                                     " - " + rec.get("massTo").asString(null) + 
                                     " " + rec.get("unitName").asString(null));
                    System.out.println("        - isActive: " + rec.get("isActive").asBoolean());
                    if (!rec.get("corrSubstanceName").isNull()) {
                        System.out.println("        - Korrespondiert zu: " + rec.get("corrSubstanceName").asString() + 
                                         " (" + rec.get("corrMassFrom").asString(null) + ")");
                    }
                }
            }
            
            System.out.println("\n=== Zusammenfassung ===");
            System.out.println("  Substanz: " + foundName + " (mmiId: " + mmiId + ")");
            System.out.println("  RxCUIs: " + rxcuis);
            System.out.println("  Verschiedene Ingredients: " + ingCount);
            System.out.println("  Verschiedene Dose Forms: " + dfCount);
            System.out.println("\n  Mögliche Probleme für niedrige Coverage:");
            System.out.println("    1. Fehlende EDQM-Mappings für Dose Forms");
            System.out.println("    2. Strength-Mismatches (verschiedene Einheiten oder Werte)");
            System.out.println("    3. Fehlende RxCUI-Codes für Ingredients");
            System.out.println("    4. Kombinationen mit anderen Ingredients, die nicht in RxNorm existieren");
            
        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

