package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.math.BigDecimal;

/**
 * @author Markus Budeus
 */
public record GraphIngredient(long substanceMmiId, String substanceName, boolean isActive, BigDecimal massFrom,
                              BigDecimal massTo, GraphUnit unit) {

	public static final String SUBSTANCE_MMI_ID = "substanceMmiId";
	public static final String SUBSTANCE_NAME = "substanceName";
	public static final String IS_ACTIVE = "isActive";
	public static final String MASS_FROM = "massFrom";
	public static final String MASS_TO = "massTo";
	public static final String UNIT = "unit";

	public GraphIngredient(MapAccessorWithDefaultValue value) {
		this(
				value.get(SUBSTANCE_MMI_ID).asLong(),
				value.get(SUBSTANCE_NAME).asString(),
				value.get(IS_ACTIVE).asBoolean(),
				GraphUtil.toBigDecimal(value.get(MASS_FROM, (String) null)),
				GraphUtil.toBigDecimal(value.get(MASS_TO, (String) null)),
				GraphUnit.from(value.get(UNIT))
		);
	}

	public Ingredient toFhirIngredient() {
		return null; // TODO
	}

}
