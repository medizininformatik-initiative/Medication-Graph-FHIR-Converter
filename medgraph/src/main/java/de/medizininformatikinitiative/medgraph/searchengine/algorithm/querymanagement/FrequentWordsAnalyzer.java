package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.*;
import java.util.regex.Pattern;

public class FrequentWordsAnalyzer {
    private final Driver driver;

    // Konstruktor: Verbindung zur Datenbank initialisieren
    public FrequentWordsAnalyzer(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    // Schließt die Verbindung
    public void close() {
        driver.close();
    }

    // Findet die häufigsten Wörter in einer Eigenschaft
    public Map<String, Integer> findMostFrequentWords(String nodeLabel, String property) {
        try (Session session = driver.session()) {
            // Abfrage: Texte aus der angegebenen Eigenschaft extrahieren!
            String query = String.format("MATCH (n:%s) RETURN n.%s AS text", nodeLabel, property);
            Result result = session.run(query);

            // Token-Zähler initialisieren
            Map<String, Integer> wordCounts = new HashMap<>();

            // Tokenisierung und Zählung
            Pattern wordPattern = Pattern.compile("\\w+"); // Tokenisiert Wörter
            while (result.hasNext()) {
                String text = result.next().get("text").asString("");
                if (text != null && !text.isEmpty()) {
                    wordPattern.matcher(text.toLowerCase()).results()
                            .map(match -> match.group()) // Alle Wörter hinzufügen
                            .forEach(word -> wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1));
                }
            }

            return wordCounts;
        }
    }

    // Gibt die häufigsten Wörter aus, sortiert nach Häufigkeit
    public void printFrequentWords(String nodeLabel, String property) {
        Map<String, Integer> wordCounts = findMostFrequentWords(nodeLabel, property);

        // Sortiere die Wörter nach Häufigkeit absteigend
        wordCounts.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Absteigend sortieren
                .forEach(entry -> System.out.println("(" + entry.getKey() + ", " + entry.getValue() + ")"));
    }

    // Main-Methode für Tests
    public static void main(String[] args) {
        FrequentWordsAnalyzer analyzer = new FrequentWordsAnalyzer("neo4j://localhost:7687", "neo4j", "...");

        // Ergebnisse für DoseForm "german" ausgeben
        System.out.println("Häufigste Wörter in DoseForm (german):");
        analyzer.printFrequentWords("DoseForm", "german");

        // Ergebnisse für DoseForm "name" ausgeben
        System.out.println("\nHäufigste Wörter in DoseForm (name):");
        analyzer.printFrequentWords("DoseForm", "name");

        System.out.println("\nHäufigste Wörter in DoseForm (type):");
        analyzer.printFrequentWords("DoseForm", "type");

        analyzer.close();
    }
}