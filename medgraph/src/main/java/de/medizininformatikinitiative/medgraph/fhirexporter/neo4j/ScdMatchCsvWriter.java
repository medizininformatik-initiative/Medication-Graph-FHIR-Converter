package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
     * Utility class to write SCD match results to a CSV file.
     *
     * Columns:
     * - Produktname
     * - MMIID
     * - SCD_Name
     * - RXCUI
     * - Score
     * - Confidence
     * - ActiveIngredients: all active ingredients from the MMI Pharmindex in the form
     *   "Name (RxCUI)", comma-separated for multi-ingredient products.
     */
public class ScdMatchCsvWriter {
    
    private static ScdMatchCsvWriter instance;
    private BufferedWriter writer;
    private boolean isInitialized = false;
    private Path outputPath;
    /**
     * Charset used for writing the CSV file.
     * Windows-1252 is chosen because tools like Excel on German/Windows systems
     * often expect this encoding and correctly display characters like '®'.
     */
    private static final Charset CSV_CHARSET = Charset.forName("Windows-1252");
    
    private ScdMatchCsvWriter() {
        // Private constructor for singleton
    }
    
    /**
     * Gets the singleton instance of the CSV writer.
     */
    public static synchronized ScdMatchCsvWriter getInstance() {
        if (instance == null) {
            instance = new ScdMatchCsvWriter();
        }
        return instance;
    }
    
    /**
     * Initializes the CSV writer with the output file path.
     * Creates the CSV file and writes the header row.
     * 
     * @param outputPath Path where the CSV file should be created (usually the desktop or export directory)
     * @throws IOException if file creation fails
     */
    public synchronized void initialize(Path outputPath) throws IOException {
        if (isInitialized) {
            return; // Already initialized
        }
        
        this.outputPath = outputPath;
        this.writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputPath.toFile()), CSV_CHARSET));
        
        // Write CSV header (keep column order stable, append new column at the end)
        writer.write("Produktname,MMIID,SCD_Name,RXCUI,Score,Confidence,ActiveIngredients");
        writer.newLine();
        writer.flush();
        
        isInitialized = true;
        System.out.println("[ScdMatchCsvWriter] CSV file initialized: " + outputPath.toAbsolutePath());
    }
    
    /**
     * Writes a single SCD match result to the CSV file.
     * 
     * @param productName Name of the German product
     * @param mmiId MMI ID of the product
     * @param scdName Name of the matched RxNorm SCD
     * @param rxcui RXCUI of the matched SCD
     * @param score Match score (0.0 - 1.0)
     * @param confidence Match confidence
     * @param activeIngredients Comma-separated list of active ingredients in the form \"Name (RxCUI)\"
     * @throws IOException if writing fails
     */
    public synchronized void writeMatch(String productName, long mmiId, String scdName,
                                       String rxcui, double score, double confidence,
                                       String activeIngredients) throws IOException {
        if (!isInitialized) {
            System.err.println("[ScdMatchCsvWriter] WARNING: Writer not initialized. Skipping match.");
            return;
        }
        
        // Escape CSV values (replace quotes and commas)
        String escapedProductName = escapeCsvValue(productName);
        String escapedScdName = escapeCsvValue(scdName);
        String escapedActiveIngredients = escapeCsvValue(activeIngredients);
        
        // Write CSV row
        writer.write(String.format("%s,%d,%s,%s,%.2f,%.2f,%s",
                escapedProductName, mmiId, escapedScdName, rxcui, score, confidence, escapedActiveIngredients));
        writer.newLine();
        writer.flush();
    }
    
    /**
     * Escapes a CSV value by enclosing it in quotes if it contains special characters.
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        // Normalize known encoding artefacts for the registered trademark symbol
        // Some toolchains / viewers may introduce sequences like "Trental¬Æ" or "TrentalÆ".
        // We try to normalize these back to a proper " ®" so the CSV is readable in Excel/LibreOffice.
        value = value
                .replace("¬Æ", " ®")
                .replace("Â®", " ®")
                .replace("Æ", " ®");

        // If value contains comma, quote, or newline, enclose in quotes and escape existing quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    /**
     * Closes the CSV writer and flushes any remaining data.
     * Should be called at the end of the export process.
     */
    public synchronized void close() {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
                System.out.println("[ScdMatchCsvWriter] CSV file closed successfully: " + outputPath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("[ScdMatchCsvWriter] Error closing CSV file: " + e.getMessage());
            } finally {
                writer = null;
                isInitialized = false;
            }
        }
    }
    
    /**
     * Resets the singleton instance (useful for testing or when starting a new export).
     */
    public static synchronized void reset() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }
    
    /**
     * Returns whether the writer has been initialized.
     */
    public boolean isInitialized() {
        return isInitialized;
    }
}

