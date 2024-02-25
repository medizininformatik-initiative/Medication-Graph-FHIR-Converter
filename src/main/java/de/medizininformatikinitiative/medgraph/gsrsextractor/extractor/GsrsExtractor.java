package de.medizininformatikinitiative.medgraph.gsrsextractor.extractor;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.gsrsextractor.GsrsApiClient;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import static de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions.*;

/**
 * This class extracts all primary CAS codes from the Neo4j database and looks them up in the GSRS api. The results are
 * stored to a csv file.
 *
 * @author Markus Budeus
 */
public class GsrsExtractor {

	public static final String OUT_FILE = "output" + File.separator + "gsrs_matches.csv";
	private static final int CONTINUE_AT = 1; // 1 for restart

	public static void main(String[] args) throws IOException, InterruptedException {

		GsrsApiClient client = new GsrsApiClient();

		try (DatabaseConnection connection = new DatabaseConnection();
		     Session session = connection.createSession()) {

			Result result = session.run(
					"MATCH (s:" + SUBSTANCE_LABEL + ")<-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + " {primary: true}]-(c:" + CAS_LABEL + ") " +
							"RETURN s.mmiId, c.code"
			);

			Iterator<GsrsResult> resultIterator = result
					.stream()
					.map(record -> {
						GsrsSearchResult sr;
						try {
							sr = client.findSubstanceByCas(record.get(1).asString());
						} catch (IOException | InterruptedException e) {
							System.err.println(
									"Failed to retrieve GSRS result for substance " +
											record.get(0).asLong() + ":" + e.getMessage());
							sr = null;
						}
						return new GsrsResult(record.get(0).asLong(), record.get(1).asString(), sr);
					})
					.filter(res -> res.object != null)
					.skip(CONTINUE_AT - 1)
					.iterator();

			writeResultsToFile(resultIterator, OUT_FILE);

		}
	}

	private static void writeResultsToFile(Iterator<GsrsResult> iterator, String path)
	throws IOException {
		File outFile = new File(path);
		int iteration = Math.max(CONTINUE_AT, 1);
		long lastRequest = 0;
		boolean cont = CONTINUE_AT > 1;
		try (FileWriter writer = new FileWriter(outFile, cont)) {
			if (!cont) writer.write("MMIID;UUID;NAME;CAS;UNII;RXCUI\n");

			while (iterator.hasNext()) {
				System.out.print("\r" + iteration + "/4653");
				iteration++;
				GsrsResult result = iterator.next();
				writer.write(toCsvLine(result));
				writer.write("\n");
				if (iteration % 60 == 0)
					writer.flush();
			}
		}
	}

	private static String toCsvLine(GsrsResult result) {
		char separator = ';';
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(result.mmiId);
		stringBuilder.append(separator);

		if (result.object instanceof GsrsSingleMatch object) {
			appendValue(stringBuilder, object.uuid);
			stringBuilder.append(separator);
			appendValue(stringBuilder, object.name);
			stringBuilder.append(separator);
			appendValue(stringBuilder, object.cas);
			stringBuilder.append(separator);
			appendValue(stringBuilder, object.unii);
			stringBuilder.append(separator);
			appendValues(stringBuilder, object.rxcui);
		} else if (result.object instanceof GsrsMultiMatch match) {
			appendValues(stringBuilder, match.uuids);
			stringBuilder.append(separator);
			stringBuilder.append(separator);
			appendValue(stringBuilder, result.cas);
			stringBuilder.append(separator);
			stringBuilder.append(separator);
		}

		return stringBuilder.toString();
	}

	private static void appendValues(StringBuilder stringBuilder, String[] values) {
		String vals = String.join("|", values);
		appendValue(stringBuilder, vals);
	}

	private static void appendValue(StringBuilder stringBuilder, String value) {
		if (value == null) return;
		stringBuilder.append('"');
		stringBuilder.append(value);
		stringBuilder.append('"');
	}

	private static class GsrsResult {
		final long mmiId;
		final String cas;
		final GsrsSearchResult object;

		private GsrsResult(long mmiId, String cas, GsrsSearchResult object) {
			this.mmiId = mmiId;
			this.cas = cas;
			this.object = object;
		}
	}

}
