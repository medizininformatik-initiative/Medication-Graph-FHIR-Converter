package de.tum.med.aiim.markusbudeus.graphdbpopulator.migrators;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions;
import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import static org.neo4j.driver.Values.parameters;

/**
 * Creates a few hardcoded UCUM unit codes.
 */
public class UcumUnitCreator {

	private final Session session;

	public UcumUnitCreator(Session session) {
		this.session = session;
	}

	public void createUnits() {
		createUnit("ug", "UG", "μg");
		createUnit("mg", "MG");
		createUnit("g", "G");
		createUnit("cm3", "CM3", "ml"); // milliliters
		createUnit("dm3", "DM3", "l"); // liters
	}

	private void createUnit(String unit, String caseInsensitiveUnit) {
		createUnit(unit, caseInsensitiveUnit, unit);
	}

	private void createUnit(String unit, String caseInsensitiveUnit, String displayUnit) {
		session.run(new Query(
				"CREATE (u:" + DatabaseDefinitions.UCUM_LABEL + " {unit: $unit, unit_ci: $unit_ci, display: $display})",
				parameters("unit", unit, "unit_ci", caseInsensitiveUnit, "display", displayUnit)
		));
	}

}
