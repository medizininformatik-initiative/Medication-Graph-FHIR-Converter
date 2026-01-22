package de.medizininformatikinitiative.medgraph.tools;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.DoseFormMapper;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphEdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Analysiert die Coverage von EDQM Dose Forms zu RxNorm Dose Forms.
 * 
 * Berechnet:
 * - Anzahl Drugs mit EDQM Dose Form
 * - Anzahl Drugs mit erfolgreichem RxNorm Mapping
 * - Coverage in Prozent
 * - Häufigste EDQM Dose Forms
 * - Erfolgreiche vs. fehlgeschlagene Mappings
 * - Anzahl unmappbarer EDQM Dose Forms
 */
public class AnalyzeDoseFormMapping {
    
    public static void main(String[] args) {
        String uri = args.length > 0 ? args[0] : "bolt://localhost:7687";
        String user = args.length > 1 ? args[1] : "neo4j";
        String password = args.length > 2 ? args[2] : System.getenv().getOrDefault("NEO4J_PASSWORD", "");
        if (password.isEmpty()) {
            System.err.println("WARNING: No Neo4j password provided!");
            System.err.println("Please set NEO4J_PASSWORD environment variable or pass as command line argument.");
            System.exit(1);
        }
        String jsonOutputFile = args.length > 3 ? args[3] : null;
        
        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
             Session session = driver.session()) {
            
            System.out.println("=== EDQM Dose Form zu RxNorm Mapping Analyse ===\n");
            
            // Initialize DoseFormMapper
            Neo4jCypherDatabase database = new Neo4jCypherDatabase(session);
            DoseFormMapper.initialize(database);
            
            // Lade alle Drugs mit EDQM Dose Forms
            System.out.println("Lade Drugs mit EDQM Dose Forms aus Neo4j...");
            List<DrugDoseFormInfo> drugs = loadDrugsWithEdqmDoseForms(session);
            System.out.println("  Gefunden: " + drugs.size() + " Drugs\n");
            
            if (drugs.isEmpty()) {
                System.out.println("Keine Drugs mit EDQM Dose Forms gefunden!");
                return;
            }
            
            // Analysiere Mappings
            System.out.println("Analysiere Mappings...");
            Map<String, EdqmDoseFormStats> edqmStats = analyzeMappings(drugs, session);
            
            // Berechne Gesamtstatistiken
            long totalDrugsWithEdqm = drugs.size();
            long totalDrugsWithMapping = drugs.stream()
                    .filter(d -> d.rxnormDoseForm != null)
                    .count();
            double coverage = totalDrugsWithEdqm > 0 
                    ? (double) totalDrugsWithMapping / totalDrugsWithEdqm * 100.0 
                    : 0.0;
            
            // Ausgabe der Ergebnisse
            printSummary(totalDrugsWithEdqm, totalDrugsWithMapping, coverage);
            printEdqmDoseFormBreakdown(edqmStats);
            printMappingSuccessFailure(edqmStats);
            printUnmappableEdqmForms(edqmStats);
            
            // Speichere JSON, falls gewünscht
            if (jsonOutputFile != null && !jsonOutputFile.isBlank()) {
                try {
                    saveResultsToJson(totalDrugsWithEdqm, totalDrugsWithMapping, coverage, edqmStats, jsonOutputFile);
                    System.out.println("\nResults saved to: " + jsonOutputFile);
                } catch (IOException e) {
                    System.err.println("Error saving JSON: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Lädt alle Drugs mit EDQM Dose Forms aus Neo4j.
     */
    private static List<DrugDoseFormInfo> loadDrugsWithEdqmDoseForms(Session session) {
        String query = "MATCH (d:" + DRUG_LABEL + ") " +
            "OPTIONAL MATCH (d)-[:" + DRUG_HAS_DOSE_FORM_LABEL + "]->(df:" + MMI_DOSE_FORM_LABEL + ") " +
            "OPTIONAL MATCH (df)-[:" + DOSE_FORM_IS_EDQM + "]->(de:" + EDQM_LABEL + ")-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(dfcs:" + CODING_SYSTEM_LABEL + ") " +
            "WHERE de IS NOT NULL " +
            "RETURN DISTINCT d.mmiId AS drugId, " +
            "       de.code AS edqmCode, " +
            "       de.name AS edqmName";
        
        List<DrugDoseFormInfo> drugs = new ArrayList<>();
        Result result = session.run(query);
        
        while (result.hasNext()) {
            Record record = result.next();
            Long drugId = record.get("drugId").asLong();
            String edqmCode = record.get("edqmCode").asString(null);
            String edqmName = record.get("edqmName").asString(null);
            
            if (edqmCode != null && edqmName != null) {
                drugs.add(new DrugDoseFormInfo(drugId, edqmCode, edqmName));
            }
        }
        
        return drugs;
    }
    
    /**
     * Analysiert die Mappings für alle Drugs.
     */
    private static Map<String, EdqmDoseFormStats> analyzeMappings(
            List<DrugDoseFormInfo> drugs, Session session) {
        
        Map<String, EdqmDoseFormStats> statsMap = new HashMap<>();
        
        for (DrugDoseFormInfo drug : drugs) {
            // Erstelle GraphEdqmPharmaceuticalDoseForm
            GraphEdqmPharmaceuticalDoseForm edqm = new GraphEdqmPharmaceuticalDoseForm(
                    drug.edqmCode,
                    "http://standardterms.edqm.eu",
                    null,
                    null,
                    drug.edqmName
            );
            
            // Versuche Mapping
            String rxnormDoseForm = DoseFormMapper.mapEdqm(edqm);
            drug.rxnormDoseForm = rxnormDoseForm;
            
            // Aktualisiere Statistiken
            EdqmDoseFormStats stats = statsMap.computeIfAbsent(
                    drug.edqmCode,
                    k -> new EdqmDoseFormStats(drug.edqmCode, drug.edqmName)
            );
            stats.totalDrugs++;
            if (rxnormDoseForm != null) {
                stats.mappedDrugs++;
            }
        }
        
        return statsMap;
    }
    
    /**
     * Gibt die Zusammenfassung aus.
     */
    private static void printSummary(long totalDrugsWithEdqm, long totalDrugsWithMapping, double coverage) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ZUSAMMENFASSUNG");
        System.out.println("=".repeat(60));
        System.out.printf("Anzahl Drugs mit EDQM Dose Form:        %10d%n", totalDrugsWithEdqm);
        System.out.printf("Anzahl Drugs mit RxNorm Mapping:          %10d%n", totalDrugsWithMapping);
        System.out.printf("Coverage:                                 %10.2f%%%n", coverage);
        System.out.println("=".repeat(60) + "\n");
    }
    
    /**
     * Gibt die Aufschlüsselung nach EDQM Dose Forms aus.
     */
    private static void printEdqmDoseFormBreakdown(Map<String, EdqmDoseFormStats> statsMap) {
        System.out.println("=".repeat(60));
        System.out.println("HÄUFIGSTE EDQM DOSE FORMS");
        System.out.println("=".repeat(60));
        
        List<EdqmDoseFormStats> sortedStats = statsMap.values().stream()
                .sorted((a, b) -> Long.compare(b.totalDrugs, a.totalDrugs))
                .limit(20)
                .collect(Collectors.toList());
        
        System.out.printf("%-50s %10s %10s %10s%n", "EDQM Dose Form", "Anzahl", "Gemappt", "Coverage");
        System.out.println("-".repeat(90));
        
        for (EdqmDoseFormStats stats : sortedStats) {
            double formCoverage = stats.totalDrugs > 0 
                    ? (double) stats.mappedDrugs / stats.totalDrugs * 100.0 
                    : 0.0;
            String displayName = stats.edqmName.length() > 48 
                    ? stats.edqmName.substring(0, 45) + "..." 
                    : stats.edqmName;
            System.out.printf("%-50s %10d %10d %9.2f%%%n", 
                    displayName, stats.totalDrugs, stats.mappedDrugs, formCoverage);
        }
        
        System.out.println("=".repeat(60) + "\n");
    }
    
    /**
     * Gibt erfolgreiche vs. fehlgeschlagene Mappings aus.
     */
    private static void printMappingSuccessFailure(Map<String, EdqmDoseFormStats> statsMap) {
        System.out.println("=".repeat(60));
        System.out.println("MAPPING ERFOLG");
        System.out.println("=".repeat(60));
        
        long totalEdqmForms = statsMap.size();
        long mappedForms = statsMap.values().stream()
                .filter(s -> s.mappedDrugs > 0)
                .count();
        long unmappedForms = totalEdqmForms - mappedForms;
        
        long totalDrugs = statsMap.values().stream()
                .mapToLong(s -> s.totalDrugs)
                .sum();
        long mappedDrugs = statsMap.values().stream()
                .mapToLong(s -> s.mappedDrugs)
                .sum();
        long unmappedDrugs = totalDrugs - mappedDrugs;
        
        System.out.println("\nNach EDQM Dose Forms:");
        System.out.printf("  Erfolgreich gemappt:     %10d (%6.2f%%)%n", 
                mappedForms, (double) mappedForms / totalEdqmForms * 100.0);
        System.out.printf("  Nicht gemappt:            %10d (%6.2f%%)%n", 
                unmappedForms, (double) unmappedForms / totalEdqmForms * 100.0);
        System.out.printf("  Gesamt:                   %10d%n", totalEdqmForms);
        
        System.out.println("\nNach Drugs:");
        System.out.printf("  Erfolgreich gemappt:     %10d (%6.2f%%)%n", 
                mappedDrugs, (double) mappedDrugs / totalDrugs * 100.0);
        System.out.printf("  Nicht gemappt:            %10d (%6.2f%%)%n", 
                unmappedDrugs, (double) unmappedDrugs / totalDrugs * 100.0);
        System.out.printf("  Gesamt:                   %10d%n", totalDrugs);
        
        System.out.println("=".repeat(60) + "\n");
    }
    
    /**
     * Gibt unmappbare EDQM Dose Forms aus.
     */
    private static void printUnmappableEdqmForms(Map<String, EdqmDoseFormStats> statsMap) {
        System.out.println("=".repeat(60));
        System.out.println("UNMAPPBARE EDQM DOSE FORMS");
        System.out.println("=".repeat(60));
        
        List<EdqmDoseFormStats> unmappable = statsMap.values().stream()
                .filter(s -> s.mappedDrugs == 0)
                .sorted((a, b) -> Long.compare(b.totalDrugs, a.totalDrugs))
                .collect(Collectors.toList());
        
        System.out.println("\nAnzahl unmappbarer EDQM Dose Forms: " + unmappable.size());
        System.out.println("\nTop 20 unmappbare EDQM Dose Forms (nach Anzahl Drugs):");
        System.out.printf("%-50s %10s%n", "EDQM Dose Form", "Anzahl Drugs");
        System.out.println("-".repeat(70));
        
        for (int i = 0; i < Math.min(20, unmappable.size()); i++) {
            EdqmDoseFormStats stats = unmappable.get(i);
            String displayName = stats.edqmName.length() > 48 
                    ? stats.edqmName.substring(0, 45) + "..." 
                    : stats.edqmName;
            System.out.printf("%-50s %10d%n", displayName, stats.totalDrugs);
        }
        
        if (unmappable.size() > 20) {
            System.out.println("  ... und " + (unmappable.size() - 20) + " weitere");
        }
        
        System.out.println("=".repeat(60));
    }
    
    /**
     * Speichert die Analyseergebnisse in einer JSON-Datei.
     * <p>
     * Wenn der Dateiname ein relativer Pfad ist, wird er relativ zum Projekt-Root aufgelöst
     * (durch Finden des Verzeichnisses, das build.gradle.kts oder gradlew enthält).
     *
     * @param totalDrugsWithEdqm Gesamtanzahl Drugs mit EDQM Dose Form
     * @param totalDrugsWithMapping Anzahl Drugs mit erfolgreichem RxNorm Mapping
     * @param coverage Coverage in Prozent
     * @param edqmStats Statistiken pro EDQM Dose Form
     * @param filename Pfad zur JSON-Ausgabedatei (relative Pfade werden vom Projekt-Root aufgelöst)
     * @throws IOException wenn das Schreiben der Datei fehlschlägt
     */
    private static void saveResultsToJson(long totalDrugsWithEdqm, long totalDrugsWithMapping,
                                          double coverage, Map<String, EdqmDoseFormStats> edqmStats,
                                          String filename) throws IOException {
        Path filePath = Paths.get(filename);
        
        // If path is relative, resolve it relative to project root
        if (!filePath.isAbsolute()) {
            Path projectRoot = findProjectRoot();
            if (projectRoot != null) {
                filePath = projectRoot.resolve(filePath).normalize();
            } else {
                // Fallback: If project root not found, try to resolve from current directory
                // and go up until we find a directory with "output" subdirectory or build.gradle.kts
                Path current = Paths.get(System.getProperty("user.dir"));
                Path root = current.getRoot();
                
                while (current != null && !current.equals(root)) {
                    Path outputDir = current.resolve("output");
                    Path buildFile = current.resolve("build.gradle.kts");
                    Path gradlew = current.resolve("gradlew");
                    
                    // Check if this looks like the project root
                    if ((Files.exists(outputDir) && Files.isDirectory(outputDir)) ||
                        Files.exists(buildFile) || Files.exists(gradlew)) {
                        filePath = current.resolve(filename).normalize();
                        break;
                    }
                    current = current.getParent();
                }
                
                // If still not found, use current directory
                if (filePath.equals(Paths.get(filename))) {
                    filePath = Paths.get(System.getProperty("user.dir")).resolve(filename).normalize();
                }
            }
        }
        
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        Map<String, Object> jsonData = new LinkedHashMap<>();
        
        // Summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalDrugsWithEdqm", totalDrugsWithEdqm);
        summary.put("totalDrugsWithMapping", totalDrugsWithMapping);
        summary.put("coverage", coverage);
        jsonData.put("summary", summary);
        
        // Mapping success/failure
        long totalEdqmForms = edqmStats.size();
        long mappedForms = edqmStats.values().stream()
                .filter(s -> s.mappedDrugs > 0)
                .count();
        long unmappedForms = totalEdqmForms - mappedForms;
        
        long mappedDrugs = edqmStats.values().stream()
                .mapToLong(s -> s.mappedDrugs)
                .sum();
        long unmappedDrugs = totalDrugsWithEdqm - mappedDrugs;
        
        Map<String, Object> mappingSuccess = new LinkedHashMap<>();
        Map<String, Object> byEdqmForms = new LinkedHashMap<>();
        byEdqmForms.put("total", totalEdqmForms);
        byEdqmForms.put("mapped", mappedForms);
        byEdqmForms.put("unmapped", unmappedForms);
        byEdqmForms.put("mappedPercentage", (double) mappedForms / totalEdqmForms * 100.0);
        byEdqmForms.put("unmappedPercentage", (double) unmappedForms / totalEdqmForms * 100.0);
        
        Map<String, Object> byDrugs = new LinkedHashMap<>();
        byDrugs.put("total", totalDrugsWithEdqm);
        byDrugs.put("mapped", mappedDrugs);
        byDrugs.put("unmapped", unmappedDrugs);
        byDrugs.put("mappedPercentage", (double) mappedDrugs / totalDrugsWithEdqm * 100.0);
        byDrugs.put("unmappedPercentage", (double) unmappedDrugs / totalDrugsWithEdqm * 100.0);
        
        mappingSuccess.put("byEdqmForms", byEdqmForms);
        mappingSuccess.put("byDrugs", byDrugs);
        jsonData.put("mappingSuccess", mappingSuccess);
        
        // EDQM Dose Form statistics
        List<Map<String, Object>> edqmFormStats = new ArrayList<>();
        List<EdqmDoseFormStats> sortedStats = edqmStats.values().stream()
                .sorted((a, b) -> Long.compare(b.totalDrugs, a.totalDrugs))
                .collect(Collectors.toList());
        
        for (EdqmDoseFormStats stats : sortedStats) {
            Map<String, Object> formData = new LinkedHashMap<>();
            formData.put("edqmCode", stats.edqmCode);
            formData.put("edqmName", stats.edqmName);
            formData.put("totalDrugs", stats.totalDrugs);
            formData.put("mappedDrugs", stats.mappedDrugs);
            formData.put("coverage", stats.totalDrugs > 0 
                    ? (double) stats.mappedDrugs / stats.totalDrugs * 100.0 
                    : 0.0);
            edqmFormStats.add(formData);
        }
        jsonData.put("edqmDoseForms", edqmFormStats);
        
        // Unmappable EDQM Dose Forms
        List<Map<String, Object>> unmappableForms = new ArrayList<>();
        List<EdqmDoseFormStats> unmappable = edqmStats.values().stream()
                .filter(s -> s.mappedDrugs == 0)
                .sorted((a, b) -> Long.compare(b.totalDrugs, a.totalDrugs))
                .collect(Collectors.toList());
        
        for (EdqmDoseFormStats stats : unmappable) {
            Map<String, Object> formData = new LinkedHashMap<>();
            formData.put("edqmCode", stats.edqmCode);
            formData.put("edqmName", stats.edqmName);
            formData.put("totalDrugs", stats.totalDrugs);
            unmappableForms.add(formData);
        }
        jsonData.put("unmappableEdqmForms", unmappableForms);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            gson.toJson(jsonData, writer);
        }
    }
    
    /**
     * Findet das Projekt-Root-Verzeichnis durch Suche nach build.gradle.kts oder gradlew.
     *
     * @return Pfad zum Projekt-Root oder null, wenn nicht gefunden
     */
    private static Path findProjectRoot() {
        Path current = Paths.get(System.getProperty("user.dir"));
        Path root = current.getRoot();
        
        // Wenn wir bereits im Projekt-Root sind (build.gradle.kts oder gradlew vorhanden)
        Path buildFile = current.resolve("build.gradle.kts");
        Path gradlew = current.resolve("gradlew");
        if (Files.exists(buildFile) || Files.exists(gradlew)) {
            return current;
        }
        
        // Wenn wir in einem Unterverzeichnis sind (z.B. medgraph/), gehe nach oben
        while (current != null && !current.equals(root)) {
            buildFile = current.resolve("build.gradle.kts");
            gradlew = current.resolve("gradlew");
            
            if (Files.exists(buildFile) || Files.exists(gradlew)) {
                return current;
            }
            
            current = current.getParent();
        }
        
        // Fallback: Wenn nichts gefunden wird, verwende das aktuelle Verzeichnis
        // und versuche relativ zu diesem zu arbeiten
        return Paths.get(System.getProperty("user.dir"));
    }
    
    /**
     * Datenstruktur für Drug-Informationen.
     */
    private static class DrugDoseFormInfo {
        final Long drugId;
        final String edqmCode;
        final String edqmName;
        String rxnormDoseForm;
        
        DrugDoseFormInfo(Long drugId, String edqmCode, String edqmName) {
            this.drugId = drugId;
            this.edqmCode = edqmCode;
            this.edqmName = edqmName;
        }
    }
    
    /**
     * Statistiken für eine EDQM Dose Form.
     */
    private static class EdqmDoseFormStats {
        final String edqmCode;
        final String edqmName;
        long totalDrugs;
        long mappedDrugs;
        
        EdqmDoseFormStats(String edqmCode, String edqmName) {
            this.edqmCode = edqmCode;
            this.edqmName = edqmName;
            this.totalDrugs = 0;
            this.mappedDrugs = 0;
        }
    }
}

