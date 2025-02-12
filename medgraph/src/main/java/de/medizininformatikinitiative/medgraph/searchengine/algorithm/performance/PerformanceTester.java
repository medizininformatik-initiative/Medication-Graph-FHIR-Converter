package de.medizininformatikinitiative.medgraph.searchengine.algorithm.performance;

import java.util.ArrayList;
import java.util.List;

import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.ApacheLuceneInitialMatchFinder_V1_unoptimiert;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.InitialMatchFinder;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.LevenshteinSearchMatchFinder;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.common.utils.MemoryAnalyzer;

public class PerformanceTester {
    private final List<InitialMatchFinder> luceneExecutors;
    private final LevenshteinSearchMatchFinder levenshteinExecutor;

    public PerformanceTester(List<InitialMatchFinder> luceneExecutors, LevenshteinSearchMatchFinder levenshteinExecutor) {
        this.luceneExecutors = luceneExecutors;
        this.levenshteinExecutor = levenshteinExecutor;
    }

    public void testPerformance(List<RefinedQuery> userQueries, int numTrials) {
        for (RefinedQuery query : userQueries) {
            System.out.println("Testing query: " + query);

            // Lucene-Executor-Implementierungen testen
            for (InitialMatchFinder luceneExecutor : luceneExecutors) {
                MemoryAnalyzer.logMemoryUsage("Vor der Ausführung von " + luceneExecutor.getClass().getSimpleName());
                List<Long> luceneTimes = new ArrayList<>();

                for (int i = 0; i < numTrials; i++) {
                    // Lucene-Messung
                    SearchQuery luceneQuery = query.toSearchQuery();
                    long luceneStart = System.nanoTime();
                    luceneExecutor.findInitialMatches(luceneQuery).count(); // Force stream evaluation
                    long luceneEnd = System.nanoTime();
                    long duration = (luceneEnd - luceneStart) / 1_000_000; // Konvertiere in Millisekunden
                    luceneTimes.add(duration);

                    // Jede einzelne Messung ausgeben
                    System.out.println("Iteration " + (i + 1) + ": " + duration + " ms");
                }
                MemoryAnalyzer.logMemoryUsage("Nach der Ausführung von " + luceneExecutor.getClass().getSimpleName());

                // Durchschnitt und Varianz berechnen
                double luceneAverage = calculateAverage(luceneTimes);
                double luceneVariance = calculateVariance(luceneTimes, luceneAverage);

                // Ausgabe mit Klassennamen der Lucene-Version
                System.out.println("Lucene Executor: " + luceneExecutor.getClass().getSimpleName());
                System.out.println("Query: " + query.getProductNameKeywords());
                System.out.println("Average Time: " + luceneAverage + " ms");
                System.out.println("Variance: " + luceneVariance + " ms²");
                System.out.println();
            }

            // Levenshtein-Executor testen
            MemoryAnalyzer.logMemoryUsage("Vor der Ausführung von Levenshtein");
            List<Long> levenshteinTimes = new ArrayList<>();
            for (int i = 0; i < numTrials; i++) {
                // Levenshtein-Messung
                SearchQuery levenshteinQuery = query.toSearchQuery();
                long levenshteinStart = System.nanoTime();
                levenshteinExecutor.findInitialMatches(levenshteinQuery).count(); // Force stream evaluation
                long levenshteinEnd = System.nanoTime();
                long duration = (levenshteinEnd - levenshteinStart) / 1_000_000; // Konvertiere in Millisekunden
                levenshteinTimes.add(duration);

                // Jede einzelne Messung ausgeben
                System.out.println("Iteration " + (i + 1) + ": " + duration + " ms");
            }
            MemoryAnalyzer.logMemoryUsage("Nach der Ausführung von Levenshtein");

            // Durchschnitt und Varianz berechnen
            double levenshteinAverage = calculateAverage(levenshteinTimes);
            double levenshteinVariance = calculateVariance(levenshteinTimes, levenshteinAverage);

            // Ergebnisse ausgeben
            System.out.println("Levenshtein Executor");
            System.out.println("Query: " + query.getProductNameKeywords());
            System.out.println("Average Time: " + levenshteinAverage + " ms");
            System.out.println("Variance: " + levenshteinVariance + " ms²");
            System.out.println();
        }
    }

    private double calculateAverage(List<Long> times) {
        return times.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    private double calculateVariance(List<Long> times, double average) {
        return times.stream()
                .mapToDouble(time -> Math.pow(time - average, 2))
                .average()
                .orElse(0.0);
    }
}