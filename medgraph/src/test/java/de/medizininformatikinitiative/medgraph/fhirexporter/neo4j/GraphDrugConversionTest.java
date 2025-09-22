package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.Catalogue;
import de.medizininformatikinitiative.medgraph.FhirExportTestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Coding;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication;
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
		Medication medication = graphDrug.toMedication();

		assertNotNull(medication);

		List<Ingredient> expectedFhirIngredients = new ArrayList<>();
		int id = 1;
		for (GraphIngredient gi : graphDrug.ingredients()) {
			List<Ingredient> converted = gi.toFhirIngredientsWithCorrespoindingIngredient(id);
			id += converted.size();
			expectedFhirIngredients.addAll(converted);
		}
		assertArrayEquals(expectedFhirIngredients.toArray(), medication.ingredient);
		assertNull(medication.manufacturer);
		assertEquals(GraphUtil.toFhirRatio(graphDrug.amount(), null, graphDrug.unit()), medication.amount);

		if (graphDrug.edqmDoseForm() != null) {
			assertArrayEquals(new Coding[] { graphDrug.edqmDoseForm().toLegacyCoding() }, medication.form.coding);
		} else if (graphDrug.mmiDoseForm() != null) {
			assertEquals(graphDrug.mmiDoseForm(), medication.form.text);
		}
	}

	@Test
	void additionalSample1() {
		GraphIngredient ingredient1 = mock();
		GraphIngredient ingredient2 = mock();
		Ingredient fhirIngredient1 = mock();
		Ingredient fhirIngredient2 = mock();
		when(ingredient1.toFhirIngredientsWithCorrespoindingIngredient(anyInt())).thenReturn(List.of(fhirIngredient1));
		when(ingredient2.toFhirIngredientsWithCorrespoindingIngredient(anyInt())).thenReturn(List.of(fhirIngredient2));

		GraphAtc graphAtc = mock();
		Coding coding = mock();
		when(graphAtc.toLegacyCoding()).thenReturn(coding);

		GraphEdqmPharmaceuticalDoseForm pdf = mock();
		Coding doseFormCoding = mock();
		when(pdf.toLegacyCoding()).thenReturn(doseFormCoding);

		GraphDrug drug = new GraphDrug(
				List.of(ingredient1, ingredient2),
				List.of(graphAtc),
				"Zum Einnehmen",
				pdf,
				new BigDecimal("2.5"),
				FhirExportTestFactory.GraphUnits.MG
		);

		Medication medication = drug.toMedication();

		assertNotNull(medication);
		assertArrayEquals(new Ingredient[] { fhirIngredient1, fhirIngredient2 }, medication.ingredient);
		assertNull(medication.manufacturer);
		assertEquals(GraphUtil.toFhirRatio(new BigDecimal("2.5"), null, FhirExportTestFactory.GraphUnits.MG), medication.amount);
		assertArrayEquals(new Coding[] { doseFormCoding }, medication.form.coding);
	}


	@Test
	void additionalSample2() {
		GraphIngredient ingredient1 = mock();
		GraphIngredient ingredient2 = mock();
		Ingredient fhirIngredient1 = mock();
		Ingredient fhirIngredient2 = mock();
		when(ingredient1.toFhirIngredientsWithCorrespoindingIngredient(anyInt())).thenReturn(List.of(fhirIngredient1));
		when(ingredient2.toFhirIngredientsWithCorrespoindingIngredient(anyInt())).thenReturn(List.of(fhirIngredient2));

		GraphAtc graphAtc = mock();
		Coding coding = mock();
		when(graphAtc.toLegacyCoding()).thenReturn(coding);

		GraphDrug drug = new GraphDrug(
				List.of(ingredient1, ingredient2),
				List.of(graphAtc),
				"Zum Einnehmen",
				null,
				new BigDecimal("2.5"),
				FhirExportTestFactory.GraphUnits.MG
		);

		Medication medication = drug.toMedication();

		assertNotNull(medication);
		assertArrayEquals(new Ingredient[] { fhirIngredient1, fhirIngredient2 }, medication.ingredient);
		assertNull(medication.manufacturer);
		assertEquals(GraphUtil.toFhirRatio(new BigDecimal("2.5"), null, FhirExportTestFactory.GraphUnits.MG), medication.amount);
		assertEquals("Zum Einnehmen", medication.form.text);
	}

	static Stream<GraphDrug> factoryGraphDrugs() {
		return Catalogue.<GraphDrug>getAllFields(FhirExportTestFactory.GraphDrugs.class, false).stream();
	}

}
