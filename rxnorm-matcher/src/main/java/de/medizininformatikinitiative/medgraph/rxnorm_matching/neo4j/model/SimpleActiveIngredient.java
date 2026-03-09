package de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.model;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphUnit;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphUtil;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.AmountRange;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.Unit;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Markus Budeus
 */
public class SimpleActiveIngredient {

	public static final String SUBSTANCE_MMI_ID = "substanceMmiId";
	public static final String SUBSTANCE_NAME = "substanceName";
	public static final String MASS_FROM = "massFrom";
	public static final String MASS_TO = "massTo";
	public static final String UNIT = "unit";
	public static final String RXCUI_CODES = "rxcuiCodes";

	private final long substanceMmiId;
	private final String substanceName;
	private final BigDecimal massFrom;
	private final BigDecimal massTo;
	private final GraphUnit unit;
	private final java.util.List<String> rxcuiCodes;

	public SimpleActiveIngredient(long substanceMmiId, String substanceName, BigDecimal massFrom, BigDecimal massTo,
	                              GraphUnit unit, java.util.List<String> rxcuiCodes) {
		this.substanceMmiId = substanceMmiId;
		this.substanceName = substanceName;
		this.massFrom = massFrom;
		this.massTo = massTo;
		this.unit = unit;
		this.rxcuiCodes = rxcuiCodes;
	}

	public SimpleActiveIngredient(MapAccessorWithDefaultValue value) {
		substanceMmiId = value.get(SUBSTANCE_MMI_ID).asLong();
		substanceName = value.get(SUBSTANCE_NAME).asString();
		massFrom = GraphUtil.toBigDecimal(value.get(MASS_FROM, (String) null));
		massTo = GraphUtil.toBigDecimal(value.get(MASS_TO, (String) null));
		unit = GraphUnit.from(value.get(UNIT));
		org.neo4j.driver.Value rxVals = value.get(RXCUI_CODES, (org.neo4j.driver.Value) null);
		rxcuiCodes = rxVals.asList(org.neo4j.driver.Value::asString);
	}

	public String getUnitName() {
		if (unit == null) return null;
		return unit.name();
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

	public List<String> getRxcuiCodes() {
		return rxcuiCodes;
	}

	public String toDetailedRxNormInfo(int indent) {
		return this + " (RXCUI: " + String.join(", ", rxcuiCodes) + ")";
	}

	@Override
	public String toString() {
		return AmountRange.ofNullableUpperEnd(massFrom, massTo,
				Unit.parse(unit != null ? unit.name() : "")) + " " + substanceName;
	}
}
