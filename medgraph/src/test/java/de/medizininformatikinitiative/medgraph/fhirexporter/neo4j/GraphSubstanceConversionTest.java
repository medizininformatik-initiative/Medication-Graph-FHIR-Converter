package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.CodingSystem;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Substance;
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
		assertEquals(IdProvider.fromSubstanceMmiId(12L), fhirSubstance.getIdPart());


		List<Coding> codings1 = GraphUtil.toCodeableConcept(List.of(code1, code2)).getCoding();
		List<Coding> codings2 = fhirSubstance.getCode().getCoding();

		assertEquals(codings1.size(), codings2.size());
		for (int i = 0; i < codings1.size(); i++) {
			assertTrue(codings1.get(i).equalsDeep(codings2.get(i)));
		}

		assertEquals("Flumazenil", fhirSubstance.getCode().getText());
	}

}