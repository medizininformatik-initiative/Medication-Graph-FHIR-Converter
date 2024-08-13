package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Coding;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class GraphAtcConversionTest extends UnitTest {

	@Test
	void sample1() {
		GraphAtc graphCode = new GraphAtc(
				"N05CD08",
				"who.org/atc",
				LocalDate.of(2024, 7, 1),
				null,
				"Midazolam"
		);

		Coding coding = graphCode.toCoding();

		assertEquals("N05CD08", coding.code);
		assertEquals("who.org/atc", coding.system);
		assertEquals("Midazolam", coding.display);
		assertEquals("2024-07-01", coding.version);
		assertFalse(coding.userSelected);
	}

}