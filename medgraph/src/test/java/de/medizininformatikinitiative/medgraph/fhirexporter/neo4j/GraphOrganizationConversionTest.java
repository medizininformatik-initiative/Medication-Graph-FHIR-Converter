package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.FhirAddress;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.Organization;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

		Organization fhirOrganization = organization.toFhirOrganization();

		assertNotNull(fhirOrganization);
		assertArrayEquals(new Identifier[] { Identifier.fromOrganizationMmiId(17L) }, fhirOrganization.identifier);
		assertEquals("John J. L. Lennon Inc.", fhirOrganization.name);
		assertArrayEquals(new String[] { "John Lennon Inc." }, fhirOrganization.alias);
		assertArrayEquals(new FhirAddress[] { address.toFhirAddress() }, fhirOrganization.address);
	}

}