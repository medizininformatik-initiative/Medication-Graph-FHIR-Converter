package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.CodeableConcept;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Coding;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Status;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication;
import de.medizininformatikinitiative.medgraph.searchengine.tools.Util;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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


	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		GraphDrug graphDrug = (GraphDrug) object;
		return Objects.equals(unit, graphDrug.unit) && Objects.equals(amount,
				graphDrug.amount) && Objects.equals(mmiDoseForm,
				graphDrug.mmiDoseForm) && Util.equalsIgnoreOrder(atcCodes,
				graphDrug.atcCodes) && Util.equalsIgnoreOrder(ingredients,
				graphDrug.ingredients) && Objects.equals(edqmDoseForm, graphDrug.edqmDoseForm);
	}

	@Override
	public int hashCode() {
		return Objects.hash(mmiDoseForm);
	}

	public Medication toMedication() {
		Medication medication = new Medication();

		if (edqmDoseForm != null) {
			medication.form = new CodeableConcept();
			medication.form.coding = new Coding[] { edqmDoseForm.toLegacyCoding() };
			medication.form.text = edqmDoseForm.getName();
		} else if (mmiDoseForm != null) {
			medication.form = new CodeableConcept();
			medication.form.text = mmiDoseForm;
		}

		int nextIngredientId = 1;
		List<Ingredient> fhirIngredients = new ArrayList<>(ingredients.size() * 2);
		for (GraphIngredient gi: ingredients) {
			List<Ingredient> converted = gi.toLegacyFhirIngredientsWithCorrespoindingIngredient(nextIngredientId);
			nextIngredientId += converted.size();
			fhirIngredients.addAll(converted);
		}
		medication.ingredient = fhirIngredients.toArray(new Ingredient[0]);
		medication.amount = GraphUtil.toLegacyFhirRatio(amount, null, unit);
		medication.code = GraphUtil.toLegacyCodeableConcept(atcCodes);
		medication.setStatus(Status.ACTIVE);
		return medication;
	}
}
