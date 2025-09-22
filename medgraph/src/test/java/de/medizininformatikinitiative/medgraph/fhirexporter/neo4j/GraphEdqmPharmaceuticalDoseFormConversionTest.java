package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class GraphEdqmPharmaceuticalDoseFormConversionTest extends UnitTest {

	@Test
	void sample1() {
		GraphEdqmPharmaceuticalDoseForm graphCode = new GraphEdqmPharmaceuticalDoseForm(
				"PDF-00010001",
				"http://standardterms.edqm.eu",
				LocalDate.of(2000, 1, 1),
				"17.1",
				"Tablet"
		);

		Coding coding = graphCode.toCoding();

		assertEquals("00010001", coding.getCode()); // Prefix removed!
		assertEquals("http://standardterms.edqm.eu", coding.getSystem());
		assertEquals("Tablet", coding.getDisplay());
		assertEquals("17.1", coding.getVersion());
		assertFalse(coding.getUserSelected());
	}

}