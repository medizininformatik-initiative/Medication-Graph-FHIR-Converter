package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Markus Budeus
 */
public class GraphOrganizationConversionTest extends UnitTest {

	@Test
	void sample1() {
		GraphAddress address = new GraphAddress("Street", "1", "0141", "Kingston", null, null);
		GraphOrganization organization = new GraphOrganization(
				17L,
				"John J. L. Lennon Inc.",
				"John Lennon Inc.",
				List.of(address)
		);

		Organization fhirOrganization = organization.toFhirOrganizaition();

		assertNotNull(fhirOrganization);
		assertEquals(IdProvider.fromOrganizationMmiId(17L), fhirOrganization.getIdPart());
		assertEquals("John J. L. Lennon Inc.", fhirOrganization.getName());
		assertEquals(
				List.of("John Lennon Inc."),
				fhirOrganization.getAlias().stream().map(PrimitiveType::getValueAsString).toList());

		assertEquals(1, fhirOrganization.getAddress().size());
		Address fa = fhirOrganization.getAddress().getFirst();

		assertEquals("Street 1\n0141 Kingston", fa.getText());
		assertEquals("0141", fa.getPostalCode());
		assertEquals("Kingston", fa.getCity());
	}

}