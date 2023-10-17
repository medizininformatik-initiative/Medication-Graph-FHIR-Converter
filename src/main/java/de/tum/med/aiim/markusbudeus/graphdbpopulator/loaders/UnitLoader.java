package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public class UnitLoader extends CatalogEntryLoader {

	/**
	 * The catalog id of the molecule units.
	 */
	private static final String UNIT_CATALOG_ID = "107";

	private static final String CATALOG_ID = "CATALOGID";
	private static final String CODE = "CODE";
	private static final String NAME = "NAME";

	private static final Map<String, UcumDefinition> ucumDefinitions = new HashMap<>();

	static {
		// (MW) hinter der Einheit steht für "Mittelwert"

		ucumDefinitions.put("ANTI-XA I.E.", new UcumDefinition("[anti'Xa'U]", "[ANTI'XA'U]", "I.E. Anti-Xa"));
		ucumDefinitions.put("BE", new UcumDefinition("12.g", "12.G", "BE")); // Broteinheit
		ucumDefinitions.put("CM2", new UcumDefinition("cm", "CM", "cm"));
		ucumDefinitions.put("G", new UcumDefinition("g", "G", "g"));
		ucumDefinitions.put("MG (MW)", new UcumDefinition("ug{(MW)}", "UG{(MW)}", "μg (MW)"));
		ucumDefinitions.put("GEWPERC", new UcumDefinition("%{m/m}", "%{M/M}", "% (m/m)"));
		ucumDefinitions.put("GMW", new UcumDefinition("g{(MW)}", "G{(MW)}", "g (MW)"));
		ucumDefinitions.put("IE", new UcumDefinition("[iU]", "[IU]", "I.E."));
		ucumDefinitions.put("IECM", new UcumDefinition("[iU]/cm2", "[IU]/CM2", "I.E./cm2"));
		ucumDefinitions.put("IEMW", new UcumDefinition("[iU]{(MW)}", "[IU]{(MW)}", "I.E. (MW)"));
		ucumDefinitions.put("KEIME", new UcumDefinition("{Keime}", "{KEIME}", "Keime"));
		ucumDefinitions.put("KEIME (MW)", new UcumDefinition("{Keime (MW)}", "{KEIME (MW)}", "Keime (MW)"));
		ucumDefinitions.put("KJ", new UcumDefinition("kJ", "KJ", "kJ"));
		ucumDefinitions.put("L", new UcumDefinition("l", "L", "l"));
		ucumDefinitions.put("MCG", new UcumDefinition("ug", "UG", "μg"));
		ucumDefinitions.put("MCGH", new UcumDefinition("ug/h", "UG/HR", "μg/h"));
		ucumDefinitions.put("MCG24H", new UcumDefinition("ug/(24.h)", "UG/(24.HR)", "μg/24h"));
		ucumDefinitions.put("MCL", new UcumDefinition("ul", "UL", "μl"));
		ucumDefinitions.put("MCMOL", new UcumDefinition("umol", "UMOL", "μmol"));
		ucumDefinitions.put("MG", new UcumDefinition("mg", "MG", "mg"));
		ucumDefinitions.put("MGCM", new UcumDefinition("mg/cm2", "MG/CM2", "mg/cm2"));
		ucumDefinitions.put("MGD", new UcumDefinition("mg/d", "MG/D", "mg/d"));
		ucumDefinitions.put("MGH", new UcumDefinition("mg/h", "MG/HR", "mg/h"));
		ucumDefinitions.put("MGMW", new UcumDefinition("mg{(MW)}", "MG{(MW)}", "mg (MW)"));
		ucumDefinitions.put("MG24H", new UcumDefinition("mg/(24.h)", "MG/(24.HR)", "mg/24h"));
		ucumDefinitions.put("MIOIE", new UcumDefinition("10^6.[iU]", "10^6.[IU]", "Mio. I.E."));
		ucumDefinitions.put("MIOKEIME", new UcumDefinition("10^6{Keime}", "10^6{KEIME}", "Mio. Keime"));
		ucumDefinitions.put("MIOKEIMEMW",
				new UcumDefinition("10^6{Keime (MW)}", "10^6{KEIME (MW)}", "Mio. Keime (MW)"));
		ucumDefinitions.put("MIOZELLEN", new UcumDefinition("10^6{Zellen}", "10^6{ZELLEN}", "Mio. Zellen"));
		ucumDefinitions.put("MIO.ZELLEN/KG KG",
				new UcumDefinition("10^6{Zellen}/kg{KG}", "10^6{ZELLEN}/KG{KG}", "Mio. Zellen/kg KG"));
		ucumDefinitions.put("MIOZELLEN/ML", new UcumDefinition("10^6{Zellen}/ml", "10^6{ZELLEN}/ML", "Mio. Zellen/ml"));
		ucumDefinitions.put("ML", new UcumDefinition("ml", "ML", "ml"));
		ucumDefinitions.put("MLMW", new UcumDefinition("ml{(MW)}", "ML{(MW)}", "ml (MW)"));
		ucumDefinitions.put("MMOL", new UcumDefinition("mmol", "MMOL", "mmol"));
		ucumDefinitions.put("MMOLL", new UcumDefinition("mmol/l", "MMOL/L", "mmol/l"));
		ucumDefinitions.put("MMOLMW", new UcumDefinition("mmol{(MW)}", "MMOL{(MW)}", "mmol (MW)"));
		ucumDefinitions.put("MPAS", new UcumDefinition("cP", "CP", "mPa·s"));
		ucumDefinitions.put("MRDKEIME", new UcumDefinition("10^9{Keime}", "10^9{KEIME}", "Mrd. Keime"));
		ucumDefinitions.put("NG", new UcumDefinition("ng", "NG", "ng"));
		ucumDefinitions.put("PERC", new UcumDefinition("%", "%", "%"));
		ucumDefinitions.put("PERCMW", new UcumDefinition("%{(MW)}", "%{(MW)}", "% (MW)"));
		ucumDefinitions.put("NL", new UcumDefinition("nl", "NL", "nl"));
		ucumDefinitions.put("STK", new UcumDefinition("{Stk.}", "{STK.}", "Stk."));
		ucumDefinitions.put("VOLPERC", new UcumDefinition("%{Vol.}", "%{VOL.}", "Vol.-%"));
		ucumDefinitions.put("ZELLEN",
				new UcumDefinition("({Zellen}/cm2){(MW)}", "({ZELLEN}/CM2){(MW)}", "Zellen/cm2 (MW)"));
	}

	public UnitLoader(Session session) throws IOException {
		super(session);
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
					"MATCH (u:" + UNIT_LABEL + " {mmiCode: $mmiCode}) " +
							"SET u:" + UCUM_LABEL + " " +
							"SET u.ucumCs = $ucumCs " +
							"SET u.ucumCi = $ucumCi " +
							"SET u.print = $print ",
					"mmiCode", mmiCode, "ucumCs", definition.caseSensitiveUnit,
					"ucumCi", definition.caseInsensitiveUnit, "print", definition.printUnit
			);
		}

		// Delete the PERCVV unit and make all relations to it instead point to VOLPERC, because it means the same
		executeQuery(
				"MATCH (v1:" + UNIT_LABEL + " {mmiCode: 'VOLPERC'}) " +
						"MATCH (v2:" + UNIT_LABEL + " {mmiCode: 'PERCVV'})-[r:" + INGREDIENT_HAS_UNIT_LABEL + "]-(i) " +
						"WITH v1, v2, r, i " +
						"CREATE (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(v1) " +
						"DELETE r " +
						"WITH DISTINCT v2 " +
						"DELETE v2"
		);
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
