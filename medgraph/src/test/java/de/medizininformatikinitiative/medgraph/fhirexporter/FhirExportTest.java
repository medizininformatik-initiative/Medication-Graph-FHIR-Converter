package de.medizininformatikinitiative.medgraph.fhirexporter;

import de.medizininformatikinitiative.medgraph.TempDirectoryTestExtension;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.Neo4jMedicationExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.Neo4jOrganizationExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.Neo4jSubstanceExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.CodeableConcept;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.FhirResource;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.medication.Medication;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.organization.Organization;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.substance.Substance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Markus Budeus
 */
@ExtendWith(TempDirectoryTestExtension.class)
public class FhirExportTest extends UnitTest {

	@Mock
	private Neo4jMedicationExporter medicationExporter;
	@Mock
	private Neo4jSubstanceExporter substanceExporter;
	@Mock
	private Neo4jOrganizationExporter organizationExporter;

	private FhirExport sut;

	@BeforeEach
	void setUp(Path path) {
		Mockito.doReturn(10).when(organizationExporter).countObjects();
		Mockito.doReturn(10).when(substanceExporter).countObjects();
		Mockito.doReturn(10).when(medicationExporter).countObjects();
		Mockito.doReturn(toStream(Organization::new, 10)).when(organizationExporter).exportObjects();
		Mockito.doReturn(toStream(Substance::new, 10)).when(substanceExporter).exportObjects();
		Mockito.doReturn(toStream(Medication::new, 10)).when(medicationExporter).exportObjects();

		sut = new FhirExport(path);
	}

	@Test
	void exportResult(Path path) throws IOException {
		sut.doExport(organizationExporter, substanceExporter, medicationExporter);
		assertEquals(sut.getMaxProgress(), sut.getProgress());
	}

	@Test
	void intermediateState() throws IOException {
		AtomicInteger exportsBeforeOrganization = new AtomicInteger(-1);
		AtomicInteger exportsBeforeSubstance = new AtomicInteger(-1);
		AtomicInteger exportsBeforeMedication = new AtomicInteger(-1);

		Mockito.doAnswer(req -> {
			exportsBeforeOrganization.set(sut.getProgress());
			return toStream(Organization::new, 10);
		}).when(organizationExporter).exportObjects();
		Mockito.doAnswer(req -> {
			exportsBeforeSubstance.set(sut.getProgress());
			return toStream(Substance::new, 10);
		}).when(substanceExporter).exportObjects();
		Mockito.doAnswer(req -> {
			exportsBeforeMedication.set(sut.getProgress());
			return toStream(Medication::new, 10);
		}).when(medicationExporter).exportObjects();

		sut.doExport(organizationExporter, substanceExporter, medicationExporter);

		assertEquals(0, exportsBeforeOrganization.get());
		assertEquals(10, exportsBeforeSubstance.get());
		assertEquals(20, exportsBeforeMedication.get());
		assertEquals(30, sut.getProgress());
	}

	private <T extends FhirResource> Stream<T> toStream(Supplier<T> generator, int limit) {
		return Stream.generate(generator)
		             .peek(t -> t.identifier = new Identifier[]{Identifier.temporaryId(UUID.randomUUID().toString())})
		             .limit(limit);
	}

}