package de.medizininformatikinitiative.medgraph.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Utility program that reads the SCD match CSV file produced by {@link
 * de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.ScdMatchCsvWriter}
 * and writes a random sample of exactly 100 matches to a new CSV file.
 *
 * The header row is preserved and all columns are kept unchanged.
 */
public class RandomScdSampleGenerator {

    // Use the same charset as the writer to avoid encoding issues with special characters.
    private static final Charset CSV_CHARSET = Charset.forName("Windows-1252");

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: RandomScdSampleGenerator <inputCsv> [outputCsv]");
            System.exit(1);
        }

        Path input = Paths.get(args[0]);
        Path output;
        if (args.length == 2) {
            output = Paths.get(args[1]);
        } else {
            String fileName = input.getFileName().toString();
            int dot = fileName.lastIndexOf('.');
            String base = (dot > 0) ? fileName.substring(0, dot) : fileName;
            String ext = (dot > 0) ? fileName.substring(dot) : ".csv";
            output = input.getParent().resolve(base + "_sample_100" + ext);
        }

        try {
            sampleCsv(input, output, 100);
            System.out.println("Random sample written to: " + output.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error while sampling CSV: " + e.getMessage());
            System.exit(2);
        }
    }

    /**
     * Reads the input CSV, randomly selects up to sampleSize data rows (without replacement),
     * and writes them with the original header to the output CSV.
     */
    private static void sampleCsv(Path input, Path output, int sampleSize) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(input, CSV_CHARSET)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        if (lines.isEmpty()) {
            throw new IOException("Input CSV is empty: " + input);
        }

        String header = lines.get(0);
        List<String> rows = new ArrayList<>(lines.subList(1, lines.size()));

        if (rows.isEmpty()) {
            throw new IOException("Input CSV contains no data rows: " + input);
        }

        // Shuffle rows to get a uniform random order
        Collections.shuffle(rows, new Random());

        int actualSampleSize = Math.min(sampleSize, rows.size());
        List<String> sample = rows.subList(0, actualSampleSize);

        try (BufferedWriter writer = Files.newBufferedWriter(output, CSV_CHARSET)) {
            writer.write(header);
            writer.newLine();
            for (String row : sample) {
                writer.write(row);
                writer.newLine();
            }
        }
    }
}


