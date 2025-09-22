package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class GraphCodeConversionTest extends UnitTest {

	@Test
	void sample1() {
		GraphCode graphCode = new GraphCode(
				"014584",
				"http://samplecodesystem.tv/",
				LocalDate.of(2024, 8, 13),
				null
		);

		Coding coding = graphCode.toCoding();

		assertEquals("014584", coding.getCode());
		assertEquals("http://samplecodesystem.tv/", coding.getSystem());
		assertNull(coding.getDisplay());
		assertEquals("2024-08-13", coding.getVersion());
		assertFalse(coding.getUserSelected());
	}

	@Test
	void sample2() {
		GraphCode graphCode = new GraphCode(
				"111111",
				"http://samplecodesystem.tv/",
				LocalDate.of(2024, 8, 13),
				"v1.7"
		);

		Coding coding = graphCode.toCoding();

		assertEquals("111111", coding.getCode());
		assertEquals("http://samplecodesystem.tv/", coding.getSystem());
		assertNull(coding.getDisplay());
		assertEquals("v1.7", coding.getVersion());
		assertFalse(coding.getUserSelected());
	}

}