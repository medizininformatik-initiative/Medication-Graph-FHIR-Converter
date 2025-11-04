package de.medizininformatikinitiative.medgraph.fhirexporter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.medizininformatikinitiative.medgraph.DI;
import de.medizininformatikinitiative.medgraph.FhirExportSinkTestBase;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Substance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link FhirServerExportSink}. This test requires a local FHIR server instance.
 *
 * @author Markus Budeus
 */
public class FhirServerExportSinkTest extends FhirExportSinkTestBase {

	private static final String URL = "http://localhost:8080/fhir";
	private FhirServerExportSink sut;

	private final FhirContext context = DI.get(FhirContext.class);
	private final IGenericClient client = context.newRestfulGenericClient(URL);

	@BeforeEach
	void setUp() {
		sut = new FhirServerExportSink(URL);
	}

	@AfterEach
	void deleteResources() {
		medicationList.forEach(resource -> {
			client.delete().resourceById("Medication", resource.getIdPart()).execute();
		});
		organizationList.forEach(resource -> {
			client.delete().resourceById("Organization", resource.getIdPart()).execute();
		});
		substanceList.forEach(resource -> {
			client.delete().resourceById("Substance", resource.getIdPart()).execute();
		});
	}

	@Test
	void exportResult() throws IOException {
		sut.doExport(new FhirExportSources(organizationExporter, substanceExporter, medicationExporter));
		assertEquals(sut.getMaxProgress(), sut.getProgress());

		medicationList.forEach(medication -> {
			assertDoesNotThrow(() -> client.read().resource(Medication.class).withId(medication.getId()).execute(),
					"Could not find Medication " + medication.getId() + " which should have been created.");
		});
		substanceList.forEach(substance -> {
			assertDoesNotThrow(() -> client.read().resource(Substance.class).withId(substance.getId()).execute(),
					"Could not find Substance " + substance.getId() + " which should have been created.");
		});
		organizationList.forEach(organization -> {
			assertDoesNotThrow(() -> client.read().resource(Organization.class).withId(organization.getId()).execute(),
					"Could not find Organization " + organization.getId() + " which should have been created.");
		});
	}

	@Test
	void withResourceInterdependencies() throws IOException {
		for (int i = 0; i < Math.min(medicationList.size(), organizationList.size()); i++) {
			organizationList.get(i).setId("org"+i);
			medicationList.get(i).setManufacturer(
					new Reference().setReference("Organization/org"+i)
			);
		}
		for (int i = 0; i < Math.min(medicationList.size(), substanceList.size()); i++) {
			substanceList.get(i).setId("sub"+i);
			medicationList.get(i).addIngredient()
					              .setItem(
					new Reference().setReference("Substance/sub"+i)
			);
		}

		exportResult();
	}

	@AfterEach
	void cleanUp() {
		medicationList.forEach(resource -> client.delete().resourceById("Medication", resource.getIdPart()));
		substanceList.forEach(resource -> client.delete().resourceById("Substance", resource.getIdPart()));
		organizationList.forEach(resource -> client.delete().resourceById("Organization", resource.getIdPart()));
	}

}