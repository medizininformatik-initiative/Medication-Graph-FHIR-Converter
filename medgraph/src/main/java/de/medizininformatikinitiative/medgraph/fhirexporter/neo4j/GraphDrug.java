package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Markus Budeus
 */
public record GraphDrug(List<GraphIngredient> ingredients, List<GraphAtc> atcCodes, String mmiDoseForm,
                        GraphEdqmPharmaceuticalDoseForm edqmDoseForm, BigDecimal amount, GraphUnit unit) {

	public static final String INGREDIENTS = "ingredients";
	public static final String ATC_CODES = "atcCodes";
	public static final String MMI_DOSE_FORM = "mmiDoseForm";
	public static final String EDQM_DOSE_FORM = "edqmDoseForm";
	public static final String AMOUNT = "amount";
	public static final String UNIT = "unit";


	public GraphDrug(MapAccessorWithDefaultValue value) {
		this(
				value.get(INGREDIENTS).asList(GraphIngredient::new),
				value.get(ATC_CODES).asList(GraphAtc::new),
				value.get(MMI_DOSE_FORM, (String) null),
				GraphEdqmPharmaceuticalDoseForm.from(value.get(EDQM_DOSE_FORM)),
				GraphUtil.toBigDecimal(value.get(AMOUNT, (String) null)),
				GraphUnit.from(value.get(UNIT))
		);
	}


}
