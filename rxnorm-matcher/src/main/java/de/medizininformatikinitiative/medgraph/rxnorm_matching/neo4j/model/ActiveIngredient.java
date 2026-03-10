package de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.model;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphUnit;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus Budeus
 */
public class ActiveIngredient extends SimpleActiveIngredient {

	public static final String CORRESPONDING_INGREDIENTS = "ci";
	@NotNull
	private final List<SimpleActiveIngredient> correspondingIngredients;

	public ActiveIngredient(MapAccessorWithDefaultValue value) {
		super(value);

		Value ci = value.get(CORRESPONDING_INGREDIENTS);
		if (ci.isNull()) {
			this.correspondingIngredients = new ArrayList<>();
		} else {
			this.correspondingIngredients = ci.asList(SimpleActiveIngredient::new);
		}
	}

	public ActiveIngredient(long substanceMmiId, String substanceName, BigDecimal massFrom,
	                        BigDecimal massTo, GraphUnit unit, java.util.List<String> rxcuiCodes) {
		this(substanceMmiId, substanceName, massFrom, massTo, unit, rxcuiCodes, new ArrayList<>());
	}

	public ActiveIngredient(long substanceMmiId, String substanceName, BigDecimal massFrom,
	                        BigDecimal massTo, GraphUnit unit, java.util.List<String> rxcuiCodes,
	                        @NotNull List<SimpleActiveIngredient> correspondingIngredients) {
		super(substanceMmiId, substanceName, massFrom, massTo, unit, rxcuiCodes);
		this.correspondingIngredients = correspondingIngredients;
	}

	public @NotNull List<SimpleActiveIngredient> getCorrespondingIngredients() {
		return correspondingIngredients;
	}

	@Override
	public String toDetailedRxNormInfo(int indent) {
		StringBuilder result = new StringBuilder(super.toDetailedRxNormInfo(indent));
		for (SimpleActiveIngredient corresponding: correspondingIngredients) {
			result.append("\n").append(" ".repeat(indent + 2)).append("- ").append(corresponding.toDetailedRxNormInfo(indent + 2));
		}
		return result.toString();
	}

	public String toStringWithCorrespondences() {
		if (correspondingIngredients.isEmpty()) return toString();

		StringBuilder result = new StringBuilder(toString());
		result.append(" [");
		for (SimpleActiveIngredient corresponding: correspondingIngredients) {
			result.append(corresponding.toString());
			result.append(" | ");
		}
		result.delete(result.length() - 3, result.length());
		result.append(']');
		return result.toString();
	}

}
