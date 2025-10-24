package de.medizininformatikinitiative.medgraph.fhirexporter;

import de.medizininformatikinitiative.medgraph.FhirExportSinkTestBase;
import de.medizininformatikinitiative.medgraph.TempDirectoryTestExtension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Substance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.notNull;

/**
 * @author Markus Budeus
 */
@ExtendWith(TempDirectoryTestExtension.class)
public class FileFhirExportSinkTest extends FhirExportSinkTestBase {

	@Mock
	private ExportFilenameGenerator filenameGenerator;

	private FileFhirExportSink sut;

	@BeforeEach
	void setUp2(Path path) {
		Mockito.when(filenameGenerator.constructFilename((Substance) notNull()))
		       .thenAnswer(r -> UUID.randomUUID().toString());
		Mockito.when(filenameGenerator.constructFilename((Organization) notNull()))
		       .thenAnswer(r -> UUID.randomUUID().toString());
		Mockito.when(filenameGenerator.constructFilename((Medication) notNull()))
		       .thenAnswer(r -> UUID.randomUUID().toString());

		sut = new FileFhirExportSink(path, filenameGenerator);
	}

	@Test
	void exportResult() throws IOException {
		sut.doExport(new FhirExportSources(organizationExporter, substanceExporter, medicationExporter));
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
		}).when(organizationExporter).export();
		Mockito.doAnswer(req -> {
			exportsBeforeSubstance.set(sut.getProgress());
			return toStream(Substance::new, 10);
		}).when(substanceExporter).export();
		Mockito.doAnswer(req -> {
			exportsBeforeMedication.set(sut.getProgress());
			return toStream(Medication::new, 10);
		}).when(medicationExporter).export();

		sut.doExport(new FhirExportSources(organizationExporter, substanceExporter, medicationExporter));

		assertEquals(0, exportsBeforeOrganization.get());
		assertEquals(1, exportsBeforeSubstance.get());
		assertEquals(2, exportsBeforeMedication.get());
		assertEquals(3, sut.getProgress());
	}

}