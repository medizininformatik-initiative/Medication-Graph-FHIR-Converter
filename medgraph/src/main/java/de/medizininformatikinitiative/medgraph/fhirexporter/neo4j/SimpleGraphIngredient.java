package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.LegacySubstanceReference;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.SubstanceReference;
import org.hl7.fhir.r4.model.Medication;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author Markus Budeus
 */
public class SimpleGraphIngredient {

	public static final String SUBSTANCE_MMI_ID = "substanceMmiId";
	public static final String SUBSTANCE_NAME = "substanceName";
	public static final String MASS_FROM = "massFrom";
	public static final String MASS_TO = "massTo";
	public static final String UNIT = "unit";

	protected final long substanceMmiId;
	protected final String substanceName;
	protected final BigDecimal massFrom;
	protected final BigDecimal massTo;
	protected final GraphUnit unit;

	public SimpleGraphIngredient(long substanceMmiId, String substanceName, BigDecimal massFrom, BigDecimal massTo,
	                             GraphUnit unit) {
		this.substanceMmiId = substanceMmiId;
		this.substanceName = substanceName;
		this.massFrom = massFrom;
		this.massTo = massTo;
		this.unit = unit;
	}

	public SimpleGraphIngredient(MapAccessorWithDefaultValue value) {
		substanceMmiId = value.get(SUBSTANCE_MMI_ID).asLong();
		substanceName = value.get(SUBSTANCE_NAME).asString();
		massFrom = GraphUtil.toBigDecimal(value.get(MASS_FROM, (String) null));
		massTo = GraphUtil.toBigDecimal(value.get(MASS_TO, (String) null));
		unit = GraphUnit.from(value.get(UNIT));
	}

	@Deprecated
	protected de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient toLegacyBasicFhirIngredient() {
		de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient ingredient = new de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient();
		ingredient.itemReference = new LegacySubstanceReference(substanceMmiId, substanceName);
		ingredient.strength = GraphUtil.toLegacyFhirRatio(massFrom, massTo, unit);
		return ingredient;
	}

	protected Medication.MedicationIngredientComponent toBasicFhirIngredient() {
		Medication.MedicationIngredientComponent ingredient = new Medication.MedicationIngredientComponent();
		ingredient.setItem(new SubstanceReference(substanceMmiId, substanceName));
		ingredient.setStrength(GraphUtil.toFhirRatio(massFrom, massTo, unit));
		return ingredient;
	}

	public long getSubstanceMmiId() {
		return substanceMmiId;
	}

	public String getSubstanceName() {
		return substanceName;
	}

	public BigDecimal getMassFrom() {
		return massFrom;
	}

	public BigDecimal getMassTo() {
		return massTo;
	}

	public GraphUnit getUnit() {
		return unit;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		SimpleGraphIngredient that = (SimpleGraphIngredient) object;
		return substanceMmiId == that.substanceMmiId && Objects.equals(substanceName,
				that.substanceName) && Objects.equals(massFrom, that.massFrom) && Objects.equals(massTo,
				that.massTo) && Objects.equals(unit, that.unit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(substanceMmiId, substanceName, massFrom, massTo, unit);
	}
}
