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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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
		// Deleting all resources as singular requests is very inefficient. But you know what? This is not productive
		// code, so I don't care!

		// Delete parents first
		medicationList.stream()
		              .filter(m -> m.getMeta().getTag("TESTONLY", "parent") != null)
		              .forEach(resource -> {
			client.delete().resourceById("Medication", resource.getIdPart()).execute();
		});
		// Then delete child medications
		medicationList.stream()
		              .filter(m -> m.getMeta().getTag("TESTONLY", "parent") == null)
		              .forEach(resource -> {
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
			organizationList.get(i).setId("org" + i);
			medicationList.get(i).setManufacturer(
					new Reference().setReference("Organization/org" + i)
			);
		}
		for (int i = 0; i < Math.min(medicationList.size(), substanceList.size()); i++) {
			substanceList.get(i).setId("sub" + i);
			medicationList.get(i).addIngredient()
			              .setItem(
					              new Reference().setReference("Substance/sub" + i).setType("Substance")
			              );
		}

		exportResult();
	}

	@Test
	void withMedicationInternalInterdependencies() throws IOException {
		// Parent first
		medicationList.get(0).addIngredient().setItem(
				new Reference().setReference("Medication/child1").setType("Medication")
		);
		tagAsParent(medicationList.get(0));
		medicationList.get(1).setId("child1");

		// Child first
		medicationList.get(2).setId("child2");
		medicationList.get(3).addIngredient().setItem(
				new Reference().setReference("Medication/child2").setType("Medication")
		);
		tagAsParent(medicationList.get(3));

		exportResult();
	}

	@Test
	@Disabled("Test takes very long. Only run on demand.")
	void largeScaleTest() throws IOException {
		int medications = 50000;
		int substances = 1000;
		Random random = new Random();

		substanceList = toStream(Substance::new, substances).peek(s -> s.setId(UUID.randomUUID().toString())).toList();
		medicationList = new ArrayList<>();
		for (int i = 0; i < medications; i++) {
			int numChildren = 0;
			int r = random.nextInt(100);
			if (r >= 99) {
				numChildren = 5;
			} else if (r >= 97) {
				numChildren = 4;
			} else if (r >= 92) {
				numChildren = 3;
			} else if (r >= 75) {
				numChildren = 2;
			} else if (r >= 70) {
				numChildren = 1;
			} else {
				medicationList.add(randomMedication(random));
				continue;
			}
			medicationList.addAll(randomMedicationWithChildren(numChildren, random));
		}

		System.out.println("Starting large scale test.");
		// No result checks, we only ensure it runs without conflicts
		sut.doExport(new FhirExportSources(organizationExporter, substanceExporter, medicationExporter));
		assertEquals(sut.getMaxProgress(), sut.getProgress());
		System.out.println("Large scale test completed, cleaning up.");
	}

	/**
	 * Constructs a sample FHIR Medication with child medications. The medication is also tagged as "TESTONLY/parent".
	 * @param children The number of child medications to have.
	 * @param random The random instance to use for the randomness.
	 */
	private List<Medication> randomMedicationWithChildren(int children, Random random) {
		if (children < 1) throw new IllegalArgumentException("You must specify a positive number of children!");
		List<Medication> medications = new ArrayList<>(children + 1);
		Medication medication = new Medication();
		medications.add(medication);
		medication.setId(UUID.randomUUID().toString());
			tagAsParent(medication);
			for (int i = 0; i < children; i++) {
				Medication child = randomMedication(random);
				medication.addIngredient()
						.setItem(new Reference()
								.setReference("Medication/"+child.getIdPart())
								.setType("Medication")
						);
				medications.add(child);
			}
		return medications;
	}

	/**
	 * Creates a random medication with a single substance ingredient.
	 */
	private Medication randomMedication(Random random) {
		Medication medication = new Medication();
		medication.setId(UUID.randomUUID().toString());

		String substanceId = substanceList.get(random.nextInt(substanceList.size())).getIdPart();
		medication.addIngredient().setItem(
				new Reference()
						.setReference("Substance/"+substanceId)
						.setType("Substance")
		);
		return medication;
	}

	/**
	 * Adds the tag TESTONLY/parent to this medication to mark it as having children. (And thus requiring deletion
	 * in the first round.)
	 */
	private void tagAsParent(Medication medication) {
		medication.getMeta().addTag("TESTONLY", "parent", "parent");
	}

	@AfterEach
	void cleanUp() {
		medicationList.forEach(resource -> client.delete().resourceById("Medication", resource.getIdPart()));
		substanceList.forEach(resource -> client.delete().resourceById("Substance", resource.getIdPart()));
		organizationList.forEach(resource -> client.delete().resourceById("Organization", resource.getIdPart()));
	}

}