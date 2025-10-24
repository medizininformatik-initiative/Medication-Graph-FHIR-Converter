package de.medizininformatikinitiative.medgraph;

import de.medizininformatikinitiative.medgraph.fhirexporter.ExportFilenameGenerator;
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExportSource;
import de.medizininformatikinitiative.medgraph.fhirexporter.FileFhirExportSink;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Substance;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.notNull;

/**
 * @author Markus Budeus
 */
public abstract class FhirExportSinkTestBase extends UnitTest {

	protected List<Medication> medicationList = new ArrayList<>();
	protected List<Substance> substanceList = new ArrayList<>();
	protected List<Organization> organizationList = new ArrayList<>();
	@Mock
	protected FhirExportSource<Medication> medicationExporter;
	@Mock
	protected FhirExportSource<Substance> substanceExporter;
	@Mock
	protected FhirExportSource<Organization> organizationExporter;

	@BeforeEach
	void setUp() {
		medicationList = toStream(Medication::new, 10).toList();
		substanceList = toStream(Substance::new, 10).toList();
		organizationList = toStream(Organization::new, 10).toList();

		Mockito.doAnswer(req -> organizationList.stream()).when(organizationExporter).export();
		Mockito.doAnswer(req -> substanceList.stream()).when(substanceExporter).export();
		Mockito.doAnswer(req -> medicationList.stream()).when(medicationExporter).export();
	}

	protected <T extends DomainResource> Stream<T> toStream(Supplier<T> generator, int limit) {
		return Stream.generate(generator)
		             .peek(t -> t.setId(UUID.randomUUID().toString()))
		             .limit(limit);
	}
}
