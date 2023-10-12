package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public class UnitLoader extends Loader {

	/**
	 * The catalog id of the molecule units.
	 */
	private static final String UNIT_CATALOG_ID = "107";

	private static final String CATALOG_ID = "CATALOGID";
	private static final String CODE = "CODE";
	private static final String NAME = "NAME";

	private static final Map<String, UcumDefinition> ucumDefinitions = new HashMap<>();

	static {
		ucumDefinitions.put("CM2", new UcumDefinition("cm", "CM", "cm"));
		ucumDefinitions.put("G", new UcumDefinition("g", "G", "g"));
		ucumDefinitions.put("GEWPERC", new UcumDefinition("%{m/m}", "%{M/M}", "% (m/m)"));
		ucumDefinitions.put("KEIME", new UcumDefinition("{Keime}", "{KEIME}", "Keime"));
		ucumDefinitions.put("KJ", new UcumDefinition("kJ", "KJ", "kJ"));
		ucumDefinitions.put("L", new UcumDefinition("l", "L", "l"));
		ucumDefinitions.put("MCG", new UcumDefinition("ug", "UG", "μg"));
		ucumDefinitions.put("MCGH", new UcumDefinition("ug/h", "UG/HR", "μg/h"));
		ucumDefinitions.put("MCL", new UcumDefinition("ul", "UL", "μl"));
		ucumDefinitions.put("MCMOL", new UcumDefinition("umol", "UMOL", "μmol"));
		ucumDefinitions.put("MG", new UcumDefinition("mg", "MG", "mg"));
		ucumDefinitions.put("MGCM", new UcumDefinition("mg/cm2", "MG/CM2", "mg/cm2"));
		ucumDefinitions.put("MGD", new UcumDefinition("mg/d", "MG/D", "mg/d"));
		ucumDefinitions.put("MGH", new UcumDefinition("mg/(24.h)", "MG/(24.HR)", "mg/24h"));
		ucumDefinitions.put("MIOKEIME", new UcumDefinition("10^6{Keime}", "10^6{KEIME}", "Mio. Keime"));
		ucumDefinitions.put("MIOZELLEN", new UcumDefinition("10^6{Zellen}", "10^6{ZELLEN}", "Mio. Zellen"));
		ucumDefinitions.put("MIO.ZELLEN/KG KG",
				new UcumDefinition("10^6{Zellen}/kg{KG}", "10^6{ZELLEN}/KG{KG}", "Mio. Zellen/kg KG"));
		ucumDefinitions.put("MIOZELLEN/ML", new UcumDefinition("10^6{Zellen}/ml", "10^6{ZELLEN}/ML", "Mio. Zellen/ml"));
		ucumDefinitions.put("ML", new UcumDefinition("ml", "ML", "ml"));
		ucumDefinitions.put("MMOL", new UcumDefinition("mmol", "MMOL", "mmol"));
		ucumDefinitions.put("MMOLL", new UcumDefinition("mmol/l", "MMOL/L", "mmol/l"));
		ucumDefinitions.put("MRDKEIME", new UcumDefinition("10^9{Keime}", "10^9{KEIME}", "Mrd. Keime"));
		ucumDefinitions.put("NG", new UcumDefinition("ng", "NG", "ng"));
		ucumDefinitions.put("PERC", new UcumDefinition("%", "%", "%"));
		ucumDefinitions.put("NL", new UcumDefinition("nl", "NL", "nl"));
		ucumDefinitions.put("STK", new UcumDefinition("{Stk.}", "{STK.}", "Stk."));
		ucumDefinitions.put("VOLPERC", new UcumDefinition("%{Vol.}", "%{VOL.}", "Vol.-%"));
	}

	public UnitLoader(Session session) throws IOException {
		super("CATALOGENTRY.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT unitMmiCodeConstraint IF NOT EXISTS FOR (u:" + UNIT_LABEL + ") REQUIRE u.mmiCode IS UNIQUE"
		);
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + row(CATALOG_ID) + " = '" + UNIT_CATALOG_ID + "' " +
						"CREATE (u:" + UNIT_LABEL +
						" {mmiCode: " + row(CODE) + ", mmiName: " + row(NAME) + "})"
		));

		for (Map.Entry<String, UcumDefinition> ucumDefinitionEntry : ucumDefinitions.entrySet()) {
			String mmiCode = ucumDefinitionEntry.getKey();
			UcumDefinition definition = ucumDefinitionEntry.getValue();
			executeQuery(
					"MATCH (u:" + UNIT_LABEL + " {mmiCode: '" + mmiCode + "'}) " +
							"SET u:" + UCUM_LABEL + " " +
							"SET u.ucumCs = '" + definition.caseSensitiveUnit + "' " +
							"SET u.ucumCi = '" + definition.caseInsensitiveUnit + "' " +
							"SET u.print = '" + definition.printUnit + "' "
			);
		}
	}

	private static class UcumDefinition {
		final String caseSensitiveUnit;
		final String caseInsensitiveUnit;
		final String printUnit;


		private UcumDefinition(String caseSensitiveUnit, String caseInsensitiveUnit, String printUnit) {
			this.caseSensitiveUnit = caseSensitiveUnit;
			this.caseInsensitiveUnit = caseInsensitiveUnit;
			this.printUnit = printUnit;
		}
	}

}
