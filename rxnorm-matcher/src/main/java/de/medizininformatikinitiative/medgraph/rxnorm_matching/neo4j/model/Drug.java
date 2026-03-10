package de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.model;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.*;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a Drug from the Neo4j knowledge graph, with associated data selected for RxNorm-Matching.
 *
 * @author Markus Budeus
 */
public record Drug(
		Long productMmiId,
		String productName,
		Long mmiId,
		List<ActiveIngredient> activeIngredients,
		String mmiDoseForm,
		GraphEdqmPharmaceuticalDoseForm edqmDoseForm,
		BigDecimal amount,
		GraphUnit unit
) {

	public static final String DRUG_MMI_ID = "drugId";
	public static final String PRODUCT_MMI_ID = "productId";
	public static final String PRODUCT_NAME = "productName";
	public static final String INGREDIENTS = "ingredients";
	public static final String MMI_DOSE_FORM = "mmiDoseForm";
	public static final String EDQM_DOSE_FORM = "edqmDoseForm";
	public static final String AMOUNT = "amount";
	public static final String UNIT = "unit";

	public Drug(MapAccessorWithDefaultValue value) {
		this(
				value.get(PRODUCT_MMI_ID).asLong(),
				value.get(PRODUCT_NAME).asString(),
				value.get(DRUG_MMI_ID).asLong(),
				value.get(INGREDIENTS).asList(ActiveIngredient::new),
				value.get(MMI_DOSE_FORM, (String) null),
				GraphEdqmPharmaceuticalDoseForm.from(value.get(EDQM_DOSE_FORM)),
				GraphUtil.toBigDecimal(value.get(AMOUNT, (String) null)),
				GraphUnit.from(value.get(UNIT))
		);
	}

	public String getUnitName() {
		if (unit == null) return null;
		return unit.name();
	}

	public @NotNull String getDetailedInfo() {
		return "Drug of <" + productMmiId + "> " + productName + "\n- " +
				String.join("\n- ",
						activeIngredients.stream().map(i -> i.toDetailedRxNormInfo(0)).toList().toArray(new String[0])) +
				"\n";
	}

	@Override
	public @NotNull String toString() {
		return "Drug of <" + productMmiId + "> " + productName + " ["
				+ String.join(", ",
				activeIngredients.stream().map(ActiveIngredient::toString).toList().toArray(new String[0])) +
				"]";
	}
}
