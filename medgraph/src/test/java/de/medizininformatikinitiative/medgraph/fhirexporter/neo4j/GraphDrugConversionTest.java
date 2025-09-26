package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.Catalogue;
import de.medizininformatikinitiative.medgraph.FhirExportTestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Markus Budeus
 */
public class GraphDrugConversionTest extends UnitTest {

	@ParameterizedTest
	@MethodSource("factoryGraphDrugs")
	void factorySampleConversion(GraphDrug graphDrug) {
		Medication medication = graphDrug.toFhirMedication();

		assertNotNull(medication);

		List<Medication.MedicationIngredientComponent> expectedFhirIngredients = new ArrayList<>();
		int id = 1;
		for (GraphIngredient gi : graphDrug.ingredients()) {
			List<Medication.MedicationIngredientComponent> converted = gi.toFhirIngredientsWithCorrespoindingIngredient(
					id);
			id += converted.size();
			expectedFhirIngredients.addAll(converted);
		}

		assertFhirEquals(expectedFhirIngredients, medication.getIngredient());
		assertFalse(medication.hasManufacturer());
		assertFhirEquals(GraphUtil.toFhirRatio(graphDrug.amount(), null, graphDrug.unit()),
				medication.hasAmount() ? medication.getAmount() : null);

		if (graphDrug.edqmDoseForm() != null) {
			assertFhirEquals(List.of(graphDrug.edqmDoseForm().toCoding()), medication.getForm().getCoding());
		} else if (graphDrug.mmiDoseForm() != null) {
			assertEquals(graphDrug.mmiDoseForm(), medication.getForm().getText());
		}
	}

	@Test
	void additionalSample1() {
		GraphIngredient ingredient1 = mock();
		GraphIngredient ingredient2 = mock();
		Medication.MedicationIngredientComponent fhirIngredient1 = mock();
		Medication.MedicationIngredientComponent fhirIngredient2 = mock();
		when(ingredient1.toFhirIngredientsWithCorrespoindingIngredient(anyInt())).thenReturn(List.of(fhirIngredient1));
		when(ingredient2.toFhirIngredientsWithCorrespoindingIngredient(anyInt())).thenReturn(List.of(fhirIngredient2));

		GraphAtc graphAtc = mock();
		Coding coding = mock();
		when(graphAtc.toCoding()).thenReturn(coding);

		GraphEdqmPharmaceuticalDoseForm pdf = mock();
		Coding doseFormCoding = mock();
		when(pdf.toCoding()).thenReturn(doseFormCoding);

		GraphDrug drug = new GraphDrug(
				List.of(ingredient1, ingredient2),
				List.of(graphAtc),
				"Zum Einnehmen",
				pdf,
				new BigDecimal("2.5"),
				FhirExportTestFactory.GraphUnits.MG
		);

		Medication medication = drug.toFhirMedication();

		assertNotNull(medication);
		assertFalse(medication.hasManufacturer());
		assertFhirEquals(GraphUtil.toFhirRatio(new BigDecimal("2.5"), null, FhirExportTestFactory.GraphUnits.MG)
				, medication.getAmount());
		assertEquals(List.of(doseFormCoding), medication.getForm().getCoding());
	}


	@Test
	void additionalSample2() {
		GraphIngredient ingredient1 = mock();
		GraphIngredient ingredient2 = mock();
		Medication.MedicationIngredientComponent fhirIngredient1 = mock();
		Medication.MedicationIngredientComponent fhirIngredient2 = mock();
		when(ingredient1.toFhirIngredientsWithCorrespoindingIngredient(anyInt())).thenReturn(List.of(fhirIngredient1));
		when(ingredient2.toFhirIngredientsWithCorrespoindingIngredient(anyInt())).thenReturn(List.of(fhirIngredient2));

		GraphAtc graphAtc = mock();
		Coding coding = mock();
		when(graphAtc.toCoding()).thenReturn(coding);

		GraphDrug drug = new GraphDrug(
				List.of(ingredient1, ingredient2),
				List.of(graphAtc),
				"Zum Einnehmen",
				null,
				new BigDecimal("2.5"),
				FhirExportTestFactory.GraphUnits.MG
		);

		Medication medication = drug.toFhirMedication();

		assertNotNull(medication);
		assertFalse(medication.hasManufacturer());
		assertFhirEquals(GraphUtil.toFhirRatio(new BigDecimal("2.5"), null, FhirExportTestFactory.GraphUnits.MG),
				medication.getAmount());
		assertEquals("Zum Einnehmen", medication.getForm().getText());
	}

	static Stream<GraphDrug> factoryGraphDrugs() {
		return Catalogue.<GraphDrug>getAllFields(FhirExportTestFactory.GraphDrugs.class, false).stream();
	}

	private void assertFhirEquals(Base expected, Base actual) {
		if (expected == null) {
			assertNull(actual);
			return;
		}
		assertTrue(expected.equalsDeep(actual));
	}

	private void assertFhirEquals(List<? extends Base> expected, List<? extends Base> actual) {
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertTrue(expected.get(i).equalsDeep(actual.get(i)));
		}
	}

}
