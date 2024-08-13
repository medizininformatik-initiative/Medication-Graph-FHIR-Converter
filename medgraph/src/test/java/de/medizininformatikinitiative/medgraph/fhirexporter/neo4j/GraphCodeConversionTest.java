package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Coding;
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

		assertEquals("014584", coding.code);
		assertEquals("http://samplecodesystem.tv/", coding.system);
		assertNull(coding.display);
		assertEquals("2024-08-13", coding.version);
		assertFalse(coding.userSelected);
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

		assertEquals("111111", coding.code);
		assertEquals("http://samplecodesystem.tv/", coding.system);
		assertNull(coding.display);
		assertEquals("v1.7", coding.version);
		assertFalse(coding.userSelected);
	}

}