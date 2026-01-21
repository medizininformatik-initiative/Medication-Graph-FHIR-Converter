package de.medizininformatikinitiative.medgraph.tools;

import org.neo4j.driver.*;
import java.util.*;

/**
 * Analyse-Tool für Levothyroxin-Natrium
 */
public class AnalyzeLevothyroxin {
    public static void main(String[] args) {
        String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost:7687");
        String user = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "7o7MP~8_)h~0");
        
        System.out.println("Connecting to Neo4j: " + uri + " as " + user);
        AuthToken token = AuthTokens.basic(user, password);
        
        try (Driver driver = GraphDatabase.driver(uri, token, Config.defaultConfig());
             Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            
            String substanceName = "Levothyroxin-Natrium";
            
            System.out.println("\n=== Schritt 1: Prüfe ob Levothyroxin-Natrium in Neo4j existiert ===");
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
            
            if (result1.hasNext()) {
                var record = result1.next();
                mmiId = record.get("s.mmiId").asLong();
                foundName = record.get("s.name").asString();
                System.out.println("  ✓ Gefunden: " + foundName + " (mmiId: " + mmiId + ")");
            } else {
                System.out.println("  ✗ Nicht gefunden: " + substanceName);
                return;
            }
            
            System.out.println("\n=== Schritt 2: Prüfe welche Ingredients mit dieser Substanz gefunden werden ===");
            String query2 = """
                MATCH (s:Substance {mmiId: $mmiId})
                MATCH (s)<-[:IS_SUBSTANCE]-(ci:Ingredient)
                OPTIONAL MATCH (ci)<-[:CORRESPONDS_TO]-(i:MmiIngredient)
                WITH s, CASE WHEN i IS NULL THEN ci ELSE i END AS ingredient 
                WHERE ingredient.isActive = true
                RETURN DISTINCT ingredient.mmiId AS ingredientId, 
                       ingredient.massFrom AS massFrom,
                       ingredient.massTo AS massTo,
                       ingredient.isActive AS isActive
                LIMIT 10
                """;
            
            Result result2 = session.run(query2, Map.of("mmiId", mmiId));
            int ingCount = 0;
            while (result2.hasNext()) {
                var record = result2.next();
                ingCount++;
                Long ingId = record.get("ingredientId").asLong();
                String massFrom = record.get("massFrom").asString(null);
                String massTo = record.get("massTo").asString(null);
                boolean isActive = record.get("isActive").asBoolean();
                System.out.println("  Ingredient #" + ingCount + ": mmiId=" + ingId + 
                                 ", massFrom=" + massFrom + ", massTo=" + massTo + 
                                 ", isActive=" + isActive);
            }
            System.out.println("  Gesamt: " + ingCount + " verschiedene Ingredients gefunden");
            
            System.out.println("\n=== Schritt 3: Prüfe Beispiel-Products mit diesen Drugs ===");
            String query3 = """
                MATCH (s:Substance {mmiId: $mmiId})
                MATCH (s)<-[:IS_SUBSTANCE]-(ci:Ingredient)
                OPTIONAL MATCH (ci)<-[:CORRESPONDS_TO]-(i:MmiIngredient)
                WITH s, CASE WHEN i IS NULL THEN ci ELSE i END AS ingredient 
                WHERE ingredient.isActive = true
                MATCH (ingredient)<-[:CONTAINS]-(d:Drug)<-[:CONTAINS]-(p:Product)
                RETURN DISTINCT p.mmiId AS productId, p.name AS productName, 
                       d.mmiId AS drugId
                LIMIT 5
                """;
            
            Result result3 = session.run(query3, Map.of("mmiId", mmiId));
            int productCount = 0;
            while (result3.hasNext()) {
                var record = result3.next();
                productCount++;
                Long productId = record.get("productId").asLong();
                String productName = record.get("productName").asString();
                Long drugId = record.get("drugId").asLong();
                System.out.println("  Product #" + productCount + ": " + productName + 
                                 " (Product ID: " + productId + ", Drug ID: " + drugId + ")");
            }
            
            System.out.println("\n=== Schritt 4: Prüfe Ingredients in einem Beispiel-Drug ===");
            if (productCount > 0) {
                result3 = session.run(query3, Map.of("mmiId", mmiId));
                if (result3.hasNext()) {
                    var record = result3.next();
                    Long exampleDrugId = record.get("drugId").asLong();
                    String exampleProductName = record.get("productName").asString();
                    
                    System.out.println("  Analysiere Drug ID " + exampleDrugId + " aus Product: " + exampleProductName);
                    
                    String query4 = """
                        MATCH (d:Drug {mmiId: $drugId})-[:CONTAINS]->(i:MmiIngredient)-[:IS_SUBSTANCE]->(s:Substance)
                        OPTIONAL MATCH (i)-[:CORRESPONDS_TO]->(ci:Ingredient)-[:IS_SUBSTANCE]->(cs:Substance)
                        OPTIONAL MATCH (i)-[:HAS_UNIT]->(u:Unit)
                        RETURN i.mmiId AS ingredientId, i.isActive AS isActive,
                               i.massFrom AS massFrom, i.massTo AS massTo,
                               s.name AS substanceName, s.mmiId AS substanceMmiId,
                               u.name AS unitName,
                               ci.massFrom AS corrMassFrom, cs.name AS corrSubstanceName
                        ORDER BY i.isActive DESC
                        """;
                    
                    Result result4 = session.run(query4, Map.of("drugId", exampleDrugId));
                    int ingInDrug = 0;
                    while (result4.hasNext()) {
                        var rec = result4.next();
                        ingInDrug++;
                        System.out.println("    Ingredient #" + ingInDrug + ":");
                        System.out.println("      - mmiId: " + rec.get("ingredientId").asLong());
                        System.out.println("      - Substanz: " + rec.get("substanceName").asString() + 
                                         " (mmiId: " + rec.get("substanceMmiId").asLong() + ")");
                        System.out.println("      - Stärke: " + rec.get("massFrom").asString(null) + 
                                         " - " + rec.get("massTo").asString(null) + 
                                         " " + rec.get("unitName").asString(null));
                        System.out.println("      - isActive: " + rec.get("isActive").asBoolean());
                        if (!rec.get("corrSubstanceName").isNull()) {
                            System.out.println("      - Korrespondiert zu: " + rec.get("corrSubstanceName").asString() + 
                                             " (" + rec.get("corrMassFrom").asString(null) + ")");
                        }
                    }
                }
            }
            
            System.out.println("\n=== Zusammenfassung ===");
            System.out.println("  Substanz: " + foundName + " (mmiId: " + mmiId + ")");
            System.out.println("  Verschiedene Ingredients: " + ingCount);
            System.out.println("  Beispiel-Products gefunden: " + productCount);
            
        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

