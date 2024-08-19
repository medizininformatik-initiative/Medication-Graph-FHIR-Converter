package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.Catalogue;
import de.medizininformatikinitiative.medgraph.FhirExportTestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Coding;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Markus Budeus
 */
public class GraphAtcConversionTest extends UnitTest {

	@ParameterizedTest
	@MethodSource("graphAtcCodes")
	void convert(GraphAtc graphAtc) {
		Coding coding = graphAtc.toCoding();

		assertEquals(graphAtc.getCode(), coding.code);
		assertEquals(graphAtc.getSystemUri(), coding.system);
		assertEquals(graphAtc.getDescription(), coding.display);
		if (graphAtc.getSystemVersion() != null) {
			assertEquals(graphAtc.getSystemVersion(), coding.version);
		} else {
			assertEquals(GraphUtil.toFhirDate(graphAtc.getSystemDate()), coding.version);
		}
		assertFalse(coding.userSelected);
	}

	static Stream<GraphAtc> graphAtcCodes() {
		return Catalogue.<GraphAtc>getAllFields(FhirExportTestFactory.GraphCodes.Atc.class, false).stream();
	}

}