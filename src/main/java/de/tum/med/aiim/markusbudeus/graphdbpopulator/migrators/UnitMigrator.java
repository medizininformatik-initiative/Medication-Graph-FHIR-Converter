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
		UCUMDefinition ucumDefinition = resolveUcumUnit(mmiCode);
		if (ucumDefinition == null) {
			session.run(new Query(
					"CREATE (u:" + DatabaseDefinitions.UNIT_LABEL +
							" {mmi_code: $mmi_code, mmi_name: $mmi_name})",
					parameters("mmi_code", mmiCode, "mmi_name", mmiName)
			));
		} else {
			session.run(new Query(
					"CREATE (u:" + DatabaseDefinitions.UNIT_LABEL + ":" + DatabaseDefinitions.UCUM_LABEL +
							" {mmi_code: $mmi_code, mmi_name: $mmi_name," +
							"ucum_cs: $ucum_cs, ucum_ci: $ucum_ci, print: $print})",
					parameters("mmi_code", mmiCode, "mmi_name", mmiName,
							"ucum_cs", ucumDefinition.caseSensitiveUnit, "ucum_ci", ucumDefinition.caseInsensitiveUnit,
							"print", ucumDefinition.printUnit)
			));
		}
	}

	private UCUMDefinition resolveUcumUnit(String mmiCode) {
		return switch (mmiCode) {
			case "CM2" -> new UCUMDefinition("cm", "CM", "cm");
			case "G" -> new UCUMDefinition("g", "G", "g");
			case "GEWPERC" -> new UCUMDefinition("%{m/m}", "%{M/M}", "% (m/m)");
			case "KEIME" -> new UCUMDefinition("{Keime}", "{KEIME}", "Keime");
			case "KJ" -> new UCUMDefinition("kJ", "KJ", "kJ");
			case "L" -> new UCUMDefinition("l", "L", "l");
			case "MCG" -> new UCUMDefinition("ug", "UG", "μg");
			case "MCGH" -> new UCUMDefinition("ug/h", "UG/HR", "μg/h");
			case "MCL" -> new UCUMDefinition("ul", "UL", "μl");
			case "MCMOL" -> new UCUMDefinition("umol", "UMOL", "μmol");
			case "MG" -> new UCUMDefinition("mg", "MG", "mg");
			case "MGCM" -> new UCUMDefinition("mg/cm2", "MG/CM2", "mg/cm2");
			case "MGD" -> new UCUMDefinition("mg/d", "MG/D", "mg/d");
			case "MGH" -> new UCUMDefinition("mg/(24.h)", "MG/(24.HR)", "mg/24h");
			case "MIOKEIME" -> new UCUMDefinition("10^6{Keime}", "10^6{KEIME}", "Mio. Keime");
			case "MIOZELLEN" -> new UCUMDefinition("10^6{Zellen}", "10^6{ZELLEN}", "Mio. Zellen");
			case "MIO.ZELLEN/KG KG" ->
					new UCUMDefinition("10^6{Zellen}/kg{KG}", "10^6{ZELLEN}/KG{KG}", "Mio. Zellen/kg KG");
			case "MIOZELLEN/ML" -> new UCUMDefinition("10^6{Zellen}/ml", "10^6{ZELLEN}/ML", "Mio. Zellen/ml");
			case "ML" -> new UCUMDefinition("ml", "ML", "ml");
			case "MMOL" -> new UCUMDefinition("mmol", "MMOL", "mmol");
			case "MMOLL" -> new UCUMDefinition("mmol/l", "MMOL/L", "mmol/l");
			case "MRDKEIME" -> new UCUMDefinition("10^9{Keime}", "10^9{KEIME}", "Mrd. Keime");
			case "NG" -> new UCUMDefinition("ng", "NG", "ng");
			case "PERC" -> new UCUMDefinition("%", "%", "%");
			case "NL" -> new UCUMDefinition("nl", "NL", "nl");
			case "STK" -> new UCUMDefinition("{Stk.}", "{STK.}", "Stk.");
			case "VOLPERC" -> new UCUMDefinition("%{Vol.}", "%{VOL.}", "Vol.-%");
			default -> null;
		};
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
