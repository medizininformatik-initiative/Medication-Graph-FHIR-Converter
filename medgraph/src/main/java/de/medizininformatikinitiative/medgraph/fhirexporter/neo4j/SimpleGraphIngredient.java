package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.SubstanceReference;
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
    public static final String RXCUI_CODES = "rxcuiCodes";

	protected final long substanceMmiId;
	protected final String substanceName;
	protected final BigDecimal massFrom;
	protected final BigDecimal massTo;
	protected final GraphUnit unit;
    // Optional list of RxNorm codes (IN/PIN etc.) attached to the substance of this ingredient
    protected final java.util.List<String> rxcuiCodes;

    public SimpleGraphIngredient(long substanceMmiId, String substanceName, BigDecimal massFrom, BigDecimal massTo,
                                 GraphUnit unit) {
		this.substanceMmiId = substanceMmiId;
		this.substanceName = substanceName;
		this.massFrom = massFrom;
		this.massTo = massTo;
		this.unit = unit;
        this.rxcuiCodes = java.util.Collections.emptyList();
	}

	public SimpleGraphIngredient(MapAccessorWithDefaultValue value) {
		substanceMmiId = value.get(SUBSTANCE_MMI_ID).asLong();
		substanceName = value.get(SUBSTANCE_NAME).asString();
		massFrom = GraphUtil.toBigDecimal(value.get(MASS_FROM, (String) null));
		massTo = GraphUtil.toBigDecimal(value.get(MASS_TO, (String) null));
		unit = GraphUnit.from(value.get(UNIT));
        org.neo4j.driver.Value rxVals = value.get(RXCUI_CODES, (org.neo4j.driver.Value) null);
        if (rxVals == null || rxVals.isNull()) {
            rxcuiCodes = java.util.Collections.emptyList();
        } else {
            rxcuiCodes = rxVals.asList(org.neo4j.driver.Value::asString);
        }
	}

	protected Ingredient toBasicFhirIngredient() {
		Ingredient ingredient = new Ingredient();
		ingredient.itemReference = new SubstanceReference(substanceMmiId, substanceName);
		ingredient.strength = GraphUtil.toFhirRatio(massFrom, massTo, unit);
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

    public java.util.List<String> getRxcuiCodes() {
        return rxcuiCodes;
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
        return Objects.hash(substanceMmiId, substanceName, massFrom, massTo, unit, rxcuiCodes);
	}
}
