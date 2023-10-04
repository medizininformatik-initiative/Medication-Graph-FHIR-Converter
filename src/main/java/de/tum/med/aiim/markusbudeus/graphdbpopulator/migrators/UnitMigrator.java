package de.tum.med.aiim.markusbudeus.graphdbpopulator.migrators;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions;
import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static org.neo4j.driver.Values.parameters;

/**
 * Extracts the molecule units from CATALOGENTRY.CSV and adds them as unit nodes to the graph database.
 */
public class UnitMigrator extends Migrator {

	private static final int CODE_INDEX = 1;
	private static final int NAME_INDEX = 5;

	private static final String MOLECULE_UNIT_CATALOG_ID = "107";

	public UnitMigrator(Path directory, Session session)
	throws IOException {
		super(directory, "CATALOGENTRY.CSV", session);
	}

	@Override
	public void migrateLine(String[] line) {
		if (line[0].equals(MOLECULE_UNIT_CATALOG_ID)) {
			addUnitNode(line[CODE_INDEX], line[NAME_INDEX]);
		}
	}

	public void addUnitNode(String mmiCode, String mmiName) {
		session.run(new Query(
				"CREATE (u:" + DatabaseDefinitions.UNIT_LABEL + " {mmi_code: $mmi_code, mmi_name: $mmi_name})",
				parameters("$mmi_code", mmiCode, "$mmi_name", mmiName)
		));
	}

	private UCUMDefinition resolveUcumUnit(String mmiCode) {
		// TODO Complete this
		switch (mmiCode) {
			case "CM2":
				return new UCUMDefinition("", "", "");
			case "G":
				return new UCUMDefinition("", "", "");
			case "GEWPERC":
				return new UCUMDefinition("", "", "");
			case "KEIME":
				return new UCUMDefinition("", "", "");
			case "KJ":
				return new UCUMDefinition("", "", "");
			case "L":
				return new UCUMDefinition("", "", "");
			case "MCG":
				return new UCUMDefinition("", "", "");
			case "MCGH":
				return new UCUMDefinition("", "", "");
			case "MCL":
				return new UCUMDefinition("", "", "");
			case "MCMOL":
				return new UCUMDefinition("", "", "");
			case "MG":
				return new UCUMDefinition("", "", "");
			case "MGCM":
				return new UCUMDefinition("", "", "");
			case "MGD":
				return new UCUMDefinition("", "", "");
			case "MGH":
				return new UCUMDefinition("", "", "");
			case "MIOKEIME":
				return new UCUMDefinition("", "", "");
			case "MIOZELLEN":
				return new UCUMDefinition("", "", "");
			case "MIO.ZELLEN/KG KG":
				return new UCUMDefinition("", "", "");
			case "MIOZELLEN/ML":
				return new UCUMDefinition("", "", "");
			case "ML":
				return new UCUMDefinition("", "", "");
			case "MMOL":
				return new UCUMDefinition("", "", "");
			case "MMOLL":
				return new UCUMDefinition("", "", "");
			case "MRDKEIME":
				return new UCUMDefinition("", "", "");
			case "NG":
				return new UCUMDefinition("", "", "");
			case "NL":
				return new UCUMDefinition("", "", "");
			case "STK":
				return new UCUMDefinition("", "", "");
			case "VOLPERC":
				return new UCUMDefinition("", "", "");
		}
		return null;
	}

	private static class UCUMDefinition {
		final String caseSensitiveUnit;
		final String caseInsensitiveUnit;
		final String printUnit;


		private UCUMDefinition(String caseSensitiveUnit, String caseInsensitiveUnit, String printUnit) {
			this.caseSensitiveUnit = caseSensitiveUnit;
			this.caseInsensitiveUnit = caseInsensitiveUnit;
			this.printUnit = printUnit;
		}
	}

}
