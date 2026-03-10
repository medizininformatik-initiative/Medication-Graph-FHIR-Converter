package de.medizininformatikinitiative.medgraph.rxnorm_matching;

import de.medizininformatikinitiative.medgraph.commandline.CommandLineExecutor;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link DoseFormMapper}-implementation using our hand-crafted dose form mapping.
 *
 * @author Markus Budeus
 */
public class CustomDoseFormMapper implements DoseFormMapper {

	private final Map<String, String> doseFormMap = readDoseFormMapFromResources();

	@Override
	public @Nullable String getRxNormDoseForm(String edqmDoseFormName) {
		return doseFormMap.get(edqmDoseFormName);
	}

	private Map<String, String> readDoseFormMapFromResources() {
		InputStream stream = CommandLineExecutor.class.getResourceAsStream("/Darreichungsformen_EDQM_RxNorm.csv");
		if (stream == null) throw new IllegalStateException("Could not find EDQM/RxNorm mapping file!");
		Map<String, String> resultMap = new HashMap<>(600);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			reader.readLine(); // Skip first line
			String line = reader.readLine();
			while (line != null) {
				readLineAndAddToMap(line, resultMap);
				line = reader.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return resultMap;
	}

	private void readLineAndAddToMap(String line, Map<String, String> map) {
		StringBuilder edqm = new StringBuilder();
		StringBuilder rxnorm = new StringBuilder();
		boolean isEdqm = true;
		boolean isQuoted = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			switch (c) {
				case '"':
					isQuoted = !isQuoted;
					break;
				case ',':
					if (!isQuoted) {
						isEdqm = false;
						break;
					}
					// Deliberate fall-through
				default:
					if (isEdqm) {
						edqm.append(c);
					} else {
						rxnorm.append(c);
					}
			}
		}

		if (!rxnorm.isEmpty()) {
			map.put(edqm.toString(), rxnorm.toString());
		}
	}

}
