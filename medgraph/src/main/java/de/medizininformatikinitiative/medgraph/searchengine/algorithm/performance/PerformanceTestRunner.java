package de.medizininformatikinitiative.medgraph.searchengine.algorithm.performance;

import de.medizininformatikinitiative.medgraph.searchengine.algorithm.RefinedQueryTestBuilder;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.*;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.ApacheLuceneInitialMatchFinder_V1_unoptimiert;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.performance.PerformanceTester;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.provider.Providers;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.util.List;

public class PerformanceTestRunner {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Verbindung zu Neo4j herstellen
        try (Driver driver = GraphDatabase.driver("neo4j://localhost:7687", AuthTokens.basic("neo4j", "..."));
             Session session = driver.session()) {

            // Liste von Lucene-Implementierungen für einfache Abrufbarkeit
            List<InitialMatchFinder> luceneExecutors = List.of(
                    //new ApacheLuceneInitialMatchFinder_V1_unoptimiert(session)
                    //new ApacheLuceneInitialMatchFinder_V2_Abfrageoptimierung(session)
                    //new ApacheLuceneInitialMatchFinder_V3_Speicheroptimierung_ByteBuffer(session)
                    //new ApacheLuceneInitialMatchFinder_V3_Speicheroptimierung_RAM(session)
                    //new ApacheLuceneInitialMatchFinder_V3_Speicheroptimierung_MMap(session)
                    //new ApacheLuceneInitialMatchFinder_V4_Threading(session)
                    new ApacheLuceneInitialMatchFinder_V4_MedicalAnalyzer(session)
                    //new ApacheLuceneInitialMatchFinder_V5_optimiert(session)

            );

            // Initialisiere den LevenshteinSearchMatchFinder (kein Closeable, wird normal instanziiert)
            LevenshteinSearchMatchFinder levenshteinExecutor = new LevenshteinSearchMatchFinder(Providers.getProductSynonyms(session));

            // PerformanceTester initialisieren
            PerformanceTester tester = new PerformanceTester(luceneExecutors, levenshteinExecutor);

            // Beispiel-Usereingaben definieren
            List<RefinedQuery> userQueries = List.of(
                    RefinedQueryTestBuilder.createExampleQuery("Aspirin"),
                    RefinedQueryTestBuilder.createExampleQuery("Ibuprofen"),
                    RefinedQueryTestBuilder.createExampleQuery("Paracetamol"),
                    RefinedQueryTestBuilder.createExampleQuery("Diclofenac"),
                    RefinedQueryTestBuilder.createExampleQuery("Metamizol")
            );

            // Performance testen
            tester.testPerformance(userQueries, 20); // # Testläufe pro Usereingabe
        }
    }
}