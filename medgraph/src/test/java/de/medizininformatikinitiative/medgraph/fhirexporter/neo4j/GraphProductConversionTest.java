package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.Catalogue;
import de.medizininformatikinitiative.medgraph.FhirExportTestFactory;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Ratio;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
			assertFalse(medication.hasManufacturer());
		} else {
			assertEquals(IdProvider.fromOrganizationMmiId(graphProduct.companyMmiId()), medication.getManufacturer().getReferenceElement().getIdPart());
			assertEquals(graphProduct.companyName(), medication.getManufacturer().getDisplay());
		}

		if (graphProduct.name() != null) {
			assertEquals(graphProduct.name(), medication.getCode().getText());
		}

		List<Coding> codings = medication.getCode().getCoding();

		// Assert all product codings are present
		assertContainsAllCodings(codings, graphProduct.codes().stream().map(GraphCode::toCoding).toList());
		// Assert all package codings are present
		for (GraphPackage graphPackage: graphProduct.packages()) {
			assertContainsAllCodings(codings, graphPackage.codes().stream().map(GraphCode::toCoding).toList());
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
		Medication drugMedication = drug.toFhirMedication();
		assertNotNull(drugMedication);

		// Asserts all drug codings are present
		List<Coding> codings = medication.getCode().getCoding();
		assertContainsAllCodings(codings, drugMedication.getCode().getCoding());

		if (drugMedication.hasAmount()) {
			assertTrue(drugMedication.getAmount().equalsDeep(medication.getAmount()));
		} else {
			assertFalse(medication.hasAmount());
		}
		for (int i = 0; i < medication.getIngredient().size(); i++) {
			assertEquals("Substance", medication.getIngredient().get(i).getItemReference().getType());
		}
	}

	private void verifyMultiDrugConversion(GraphProduct product, List<Medication> medicationList) {
		assertEquals(product.drugs().size() + 1, medicationList.size());

		Medication primary = medicationList.getFirst();

		// Make sure ingredients are Medication objects which reference the drugs
		List<String> ingredientIdentifiers = new LinkedList<>();
		for (int i = 0; i < primary.getIngredient().size(); i++) {
			assertEquals("Medication", primary.getIngredient().get(i).getItemReference().getType());
			ingredientIdentifiers.add(primary.getIngredient().get(i).getItemReference().getReferenceElement().getIdPart());
		}

		// Check each drug medication object
		for (int i = 0; i < product.drugs().size(); i++) {
			Medication drugMedication = medicationList.get(i + 1);
			// Ensure the identifier matches whatever identifier is referenced by the parent (i.e. primary) medication
			assertEquals(ingredientIdentifiers.get(i), drugMedication.getIdPart());

			Ratio amount = product.drugs().get(i).toFhirMedication().getAmount();
			assertTrue(amount.equalsDeep(drugMedication.getAmount()));
		}
	}

	static Stream<GraphProduct> factoryProducts() {
		return Catalogue.<GraphProduct>getAllFields(FhirExportTestFactory.GraphProducts.class).stream();
	}

	private void assertContainsAllCodings(List<Coding> list, List<Coding> mustContain) {
		mustContain.forEach(coding -> {
			assertTrue(list.stream().anyMatch(c -> c.equalsDeep(coding)));
		});
	}
}
