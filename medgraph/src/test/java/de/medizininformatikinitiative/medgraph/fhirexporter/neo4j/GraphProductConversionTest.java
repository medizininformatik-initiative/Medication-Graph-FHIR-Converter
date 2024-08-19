package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.Catalogue;
import de.medizininformatikinitiative.medgraph.FhirExportTestFactory;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class GraphProductConversionTest {

	@ParameterizedTest
	@MethodSource("factoryProducts")
	void factoryConversion(GraphProduct graphProduct) {
		List<Medication> medicationList = graphProduct.toFhirMedications();

		assertNotNull(medicationList);

		Medication medication = medicationList.getFirst();
		assertNotNull(medication);

		if (graphProduct.companyMmiId() == null) {
			assertNull(medication.manufacturer);
		} else {
			assertEquals(Identifier.fromOrganizationMmiId(graphProduct.companyMmiId()),
					medication.manufacturer.identifier);
			assertEquals(graphProduct.companyName(), medication.manufacturer.display);
		}

		if (graphProduct.name() != null) {
			assertTrue(Arrays.stream(medication.identifier).anyMatch(i -> i.value.equals(graphProduct.name())));
		}

		if (graphProduct.drugs().size() == 1) {
			assertEquals(1, medicationList.size());
			verifySingleDrugConversion(graphProduct, medication);
		} else {
			verifyMultiDrugConversion(graphProduct, medicationList);
		}
	}

	private void verifySingleDrugConversion(GraphProduct product, Medication medication) {
		GraphDrug drug = product.drugs().getFirst();
		Medication drugMedication = drug.toMedication();

		assertEquals(drugMedication.amount, medication.amount);
		for (int i = 0; i < medication.ingredient.length; i++) {
			assertEquals("Substance", medication.ingredient[i].itemReference.type);
		}
	}

	private void verifyMultiDrugConversion(GraphProduct product, List<Medication> medicationList) {
		assertEquals(product.drugs().size() + 1, medicationList.size());

		Medication primary = medicationList.getFirst();

		// Make sure ingredients are Medication objects which reference the drugs
		List<String> ingredientIdentifiers = new LinkedList<>();
		for (int i = 0; i < primary.ingredient.length; i++) {
			assertEquals("Medication", primary.ingredient[i].itemReference.type);
			ingredientIdentifiers.add(primary.ingredient[i].itemReference.identifier.value);
		}

		// Check each drug medication object
		for (int i = 0; i < product.drugs().size(); i++) {
			Medication drugMedication = medicationList.get(i + 1);
			// Ensure the identifier matches whatever identifier is referenced by the parent (i.e. primary) medication
			assertEquals(ingredientIdentifiers.get(i), drugMedication.identifier[0].value);
			assertEquals(product.drugs().get(i).toMedication().amount, drugMedication.amount);
		}
	}

	static Stream<GraphProduct> factoryProducts() {
		return Catalogue.<GraphProduct>getAllFields(FhirExportTestFactory.GraphProducts.class).stream();
	}
}
