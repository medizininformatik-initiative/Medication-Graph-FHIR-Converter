package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.CSVReader;
import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.SUBSTANCE_LABEL;
import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.SYNONYME_LABEL;
import static org.neo4j.driver.Values.parameters;

public class HouselistMatcher {

	private static final String HOUSELIST = "Hausliste_mitverrausch.csv";

	private final Session session;

	/**
	 * Maps known synonymes to the mmiId of the target object
	 */
	private final Map<String, Long> substanceSynonymes;

	public static void main(String[] args) {
		DatabaseConnection.runSession(session -> {
			HouselistMatcher matcher = new HouselistMatcher(session);
			try {
				List<HouselistEntry> houselist = HouselistMatcher.loadHouselist();
				houselist.removeIf(entry -> entry.noisySubstanceName.equals(entry.substanceName));
				Map<HouselistEntry, SubstanceMatch> matches = matcher.performMatching(houselist);
				houselist.forEach(houselistEntry -> {
					SubstanceMatch match = matches.get(houselistEntry);
					System.out.print("[" + houselistEntry.noisySubstanceName + " ("+houselistEntry.substanceName +")]->");
					if (match != null) {
						System.out.println("[" + match.mmiId + ", " + match.name + "]");
					} else {
						System.out.println("[???]");
					}
				});
				int size = houselist.size();
				int matchSize = matches.size();
				DecimalFormat format = new DecimalFormat("0.0");
				System.out.println("Matched " + matchSize + "/" + size + " entries. (" + format.format(
						100.0 * matchSize / size) + "%)");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public HouselistMatcher(Session session) {
		this.session = session;
		this.substanceSynonymes = downloadSynonymes();
	}

	private OptionalLong match(HouselistEntry entry) {
		Long match = substanceSynonymes.get(entry.noisySubstanceName.toLowerCase());
		return match != null ? OptionalLong.of(match) : OptionalLong.empty();
	}


	private Map<String, Long> downloadSynonymes() {
		Map<String, Long> result = new HashMap<>();
		Result res = session.run(
				"MATCH (sy:" + SYNONYME_LABEL + ")--(s:" + SUBSTANCE_LABEL + ") WHERE s.mmiId IS NOT NULL " +
						"RETURN sy.name, s.mmiId"
		);

		res.forEachRemaining(record -> result.put(record.get(0).asString(), record.get(1).asLong()));
		return result;
	}

	public Map<HouselistEntry, SubstanceMatch> performMatching(List<HouselistEntry> entries) throws IOException {
		Map<HouselistEntry, Long> matchMap = new HashMap<>();
		for (HouselistEntry entry : entries) {
			OptionalLong match = match(entry);
			if (match.isPresent()) {
				matchMap.put(entry, match.getAsLong());
			}
		}

		Map<Long, SubstanceMatch> substances = resolveSubstances(new ArrayList<>(matchMap.values()));

		Map<HouselistEntry, SubstanceMatch> result = new HashMap<>();
		for (Map.Entry<HouselistEntry, Long> entry : matchMap.entrySet()) {
			SubstanceMatch match = substances.get(entry.getValue());
			if (match != null) {
				result.put(entry.getKey(), match);
			} else {
				System.err.println("Failed to lookup substance " + entry.getValue() + "!");
			}
		}

		return result;
	}

	private Map<Long, SubstanceMatch> resolveSubstances(List<Long> mmiIds) {
		Result result = session.run(new Query(
				"MATCH (s:" + SUBSTANCE_LABEL + ") WHERE s.mmiId IN $mmiIds " +
						"RETURN s.mmiId, s.name",
				parameters("mmiIds", mmiIds)));

		Map<Long, SubstanceMatch> res = new HashMap<>();
		while (result.hasNext()) {
			Record record = result.next();
			long mmiId = record.get(0).asLong();
			res.put(mmiId, new SubstanceMatch(mmiId, record.get(1).asString()));
		}
		return res;
	}

	@SuppressWarnings("ConstantConditions")
	public static List<HouselistEntry> loadHouselist() throws IOException {
		try (InputStream inputStream = HouselistMatcher.class.getClassLoader().getResourceAsStream(HOUSELIST);
		     InputStreamReader reader = new InputStreamReader(inputStream)) {
			CSVReader csvReader = CSVReader.open(reader);

			csvReader.readNext(); // Skip headline

			List<HouselistEntry> list = new ArrayList<>();
			String[] line;
			while ((line = csvReader.readNext()) != null) {
				list.add(new HouselistEntry(
						line[0],
						line[1],
						line[2],
						line[3],
						line[4]
				));
			}
			return list;
		}
	}
}
