package de.medizininformatikinitiative.medgraph.graphdbpopulator;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.common.CSVReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Markus Budeus
 */
public class DoseFormSynonymsFileTest extends UnitTest {

	/**
	 * This test ensures that all EDQM Concepts referenced in dose_form_synonyms.csv actually exist in
	 * edqm_objects.csv.
	 */
	@Test
	void allHandwrittenDoseFormSynonymsExist() throws IOException {
		Set<EdqmObject> edqmObjects = new HashSet<>();

		try (CSVReader objectsReader = CSVReader.open(GraphDbPopulatorSupport.class.getResourceAsStream("/edqm_objects.csv"))) {
			objectsReader.readAll().stream().skip(1).forEach(line -> {
				edqmObjects.add(new EdqmObject(line[0] + "-" + line[1], line[2]));
			});
		}

		try (CSVReader synonymsReader = CSVReader.open(
				GraphDbPopulatorSupport.class.getResourceAsStream("/dose_form_synonyms.csv"))) {
			synonymsReader.readAll().stream().skip(1).forEach(line -> {
				String targetCode = line[1];
				String targetName = line[2];
				EdqmObject expectedObject = new EdqmObject(targetCode, targetName);
				assertTrue(edqmObjects.contains(expectedObject),
						"Object \""+targetCode+"\" ("+targetName+") mentioned in the dose_form_synonyms.csv file " +
								"does not exist in the edqm_objects.csv file!"
				);
			});
		}
	}

	private static class EdqmObject {
		final String code;
		final String name;

		private EdqmObject(String code, String name) {
			this.code = code;
			this.name = name;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) return true;
			if (object == null || getClass() != object.getClass()) return false;
			EdqmObject that = (EdqmObject) object;
			return Objects.equals(code, that.code) && Objects.equals(name, that.name);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(code);
		}
	}

}
