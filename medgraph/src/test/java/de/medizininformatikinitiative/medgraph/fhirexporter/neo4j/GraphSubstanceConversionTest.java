package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.Substance;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.CodingSystem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class GraphSubstanceConversionTest extends UnitTest {

	@Test
	void sample1() {
		GraphCode code1 = new GraphCode("00002", CodingSystem.ASK.uri, CodingSystem.ASK.dateOfRetrieval, null);
		GraphCode code2 = new GraphCode("6584HL54", "someNefariousCodingSystem", LocalDate.of(2124, 11, 12),
				"3.14159265");

		GraphSubstance graphSubstance = new GraphSubstance(
				12L,
				"Flumazenil",
				List.of(code1, code2)
		);

		Substance fhirSubstance = graphSubstance.toFhirSubstance();

		assertNotNull(fhirSubstance);
		assertArrayEquals(new Identifier[] { Identifier.fromSubstanceMmiId(12L) }, fhirSubstance.identifier);
		assertArrayEquals(GraphUtil.toCodeableConcept(List.of(code1, code2)).coding, fhirSubstance.code.coding);
		assertEquals("Flumazenil", fhirSubstance.description);
	}

}