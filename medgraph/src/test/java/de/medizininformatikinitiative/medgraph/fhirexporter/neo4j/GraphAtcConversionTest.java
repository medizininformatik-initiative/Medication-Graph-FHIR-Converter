package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.Catalogue;
import de.medizininformatikinitiative.medgraph.FhirExportTestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import org.hl7.fhir.r4.model.Coding;
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

		assertEquals(graphAtc.getCode(), coding.getCode());
		assertEquals(graphAtc.getSystemUri(), coding.getSystem());
		assertEquals(graphAtc.getDescription(), coding.getDisplay());
		if (graphAtc.getSystemVersion() != null) {
			assertEquals(graphAtc.getSystemVersion(), coding.getVersion());
		} else {
			assertEquals(GraphUtil.toFhirDate(graphAtc.getSystemDate()), coding.getVersion());
		}
		assertFalse(coding.getUserSelected());
	}

	static Stream<GraphAtc> graphAtcCodes() {
		return Catalogue.<GraphAtc>getAllFields(FhirExportTestFactory.GraphCodes.Atc.class, false).stream();
	}

}