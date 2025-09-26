package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.Catalogue;
import de.medizininformatikinitiative.medgraph.FhirExportTestFactory;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Coding;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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
			assertEquals(graphProduct.name(), medication.code.text);
		}

		List<Coding> codings = Arrays.asList(medication.code.coding);
		// Assert all product codings are present
		assertTrue(codings.containsAll(graphProduct.codes().stream().map(GraphCode::toLegacyCoding).toList()));
		// Assert all package codings are present
		for (GraphPackage graphPackage: graphProduct.packages()) {
			assertTrue(codings.containsAll(graphPackage.codes().stream().map(GraphCode::toLegacyCoding).toList()));
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
		Medication drugMedication = drug.toLegacyMedication();
		assertNotNull(drugMedication);

		// Asserts all drug codings are present
		List<Coding> codings = Arrays.asList(medication.code.coding);
		if (drugMedication.code != null) {
			assertTrue(codings.containsAll(Arrays.asList(drugMedication.code.coding)));
		}

		assertEquals(drugMedication.amount, medication.amount);
		for (int i = 0; i < medication.ingredient.length; i++) {
			assertTrue(medication.ingredient[i].itemReference.type.endsWith("Substance"));
		}
	}

	private void verifyMultiDrugConversion(GraphProduct product, List<Medication> medicationList) {
		assertEquals(product.drugs().size() + 1, medicationList.size());

		Medication primary = medicationList.getFirst();

		// Make sure ingredients are Medication objects which reference the drugs
		List<String> ingredientIdentifiers = new LinkedList<>();
		for (int i = 0; i < primary.ingredient.length; i++) {
			assertTrue(primary.ingredient[i].itemReference.type.endsWith("Medication"));
			ingredientIdentifiers.add(primary.ingredient[i].itemReference.identifier.value);
		}

		// Check each drug medication object
		for (int i = 0; i < product.drugs().size(); i++) {
			Medication drugMedication = medicationList.get(i + 1);
			// Ensure the identifier matches whatever identifier is referenced by the parent (i.e. primary) medication
			assertEquals(ingredientIdentifiers.get(i), drugMedication.identifier[0].value);
			assertEquals(Objects.requireNonNull(product.drugs().get(i).toLegacyMedication()).amount, drugMedication.amount);
		}
	}

	static Stream<GraphProduct> factoryProducts() {
		return Catalogue.<GraphProduct>getAllFields(FhirExportTestFactory.GraphProducts.class).stream();
	}
}
