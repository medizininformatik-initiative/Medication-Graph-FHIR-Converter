package de.tum.med.aiim.markusbudeus.gsrsextractor.refiner;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.CSVReader;
import de.tum.med.aiim.markusbudeus.gsrsextractor.GsrsApiClient;
import de.tum.med.aiim.markusbudeus.gsrsextractor.extractor.CSVWriter;
import de.tum.med.aiim.markusbudeus.gsrsextractor.extractor.GsrsExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This class searches the file produced by the {@link GsrsExtractor} for multi-matches, i.e. CAS codes which matches to
 * multiple GSRS substances. Then, it queries those substances to find out who uses the searched CAS code as its primary
 * code.
 */
public class UuidAmbiguityResolver {

	private static final String OUT_FILE = "output" + File.separator + "gsrs_unambiguous.csv";

	private final GsrsApiClient apiClient = new GsrsApiClient();

	public static void main(String[] args) throws IOException, InterruptedException {

//		Map<Long, String> map = new HashMap<>();
//		try (DatabaseConnection connection = new DatabaseConnection();
//		     Session session = connection.createSession()) {
//			Result result = session.run("MATCH (s:Substance)<-[:REFERENCES {primary: true}]-(c:CAS) " +
//					"RETURN s.mmiId, c.code");
//			result.forEachRemaining(record -> map.put(record.get(0).asLong(), record.get(1).asString()));
//		}
//
//		try (CSVReader reader = CSVReader.open(Path.of(GsrsExtractor.OUT_FILE));
//		     CSVWriter writer = CSVWriter.open(Path.of(GsrsExtractor.OUT_FILE + "2"));
//		) {
//			String[] line;
//			while ((line = reader.readNext()) != null) {
//				if (line[3].isEmpty()) {
//					line[3] = map.get(Long.parseLong(line[0]));
//				}
//				writer.write(line);
//			}
//		}

		new UuidAmbiguityResolver().execute();
	}

	protected void execute() throws IOException, InterruptedException {
		try (CSVReader reader = CSVReader.open(Path.of(GsrsExtractor.OUT_FILE));
		     CSVWriter writer = CSVWriter.open(Path.of(OUT_FILE))) {

			List<String[]> lines = reader.readAll();
			int lineNo = 1;
			for (String[] line : lines) {
				System.out.print("\r" + lineNo + "/" + lines.size());
				System.out.flush();
				String[] result = process(line);
				if (result != null) {
					writer.write(process(line));
				}
				lineNo++;
			}
			System.out.println();
		}
	}

	private String[] process(String[] line) throws IOException, InterruptedException {
		if (line[1].equals("UUID")) {
			return new String[]{
					"MMIID", "UUID", "NAME", "CAS", "UNII", "RXCUI", "ALTCAS", "ALTRXCUI"
			};
		}
		if (line[1].contains("|")) {
			// Requires disambiguation
			return disambiguate(line);
		} else {
			// Nothing to do
			return line;
		}
	}

	private String[] disambiguate(String[] line) throws IOException, InterruptedException {
		String[] uuids = line[1].split("\\|");

		List<GsrsObject> matches = new ArrayList<>();

		for (String uuid : uuids) {
			GsrsObject object = apiClient.getObject(uuid);
			if (object == null) continue;

			if (line[3].equals(object.primaryCas)) {
				matches.add(object);
			}
		}

		if (matches.size() != 1) {
			matches.removeIf(object -> !"approved".equals(object.status));
		}
		if (matches.size() != 1) {
			matches.removeIf(object -> "concept".equals(object.substanceClass));
		}

		if (matches.size() != 1) return null; // Not successful
		return toCsv(line, matches.get(0));
	}

	private String[] toCsv(String[] line, GsrsObject object) {
		return new String[]{
				line[0],
				object.uuid,
				object.name,
				line[3],
				object.unii,
				object.primaryRxcui,
				String.join("|", object.alternativeCas),
				String.join("|", object.alternativeRxcui)
		};
	}

}
