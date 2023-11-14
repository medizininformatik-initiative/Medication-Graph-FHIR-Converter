package de.tum.med.aiim.markusbudeus.fhirexporter.neo4j;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.CodeableConcept;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Coding;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Identifier;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.organization.Address;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.organization.Organization;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance.Substance;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public class Neo4jOrganizationExporter {
	private final Session session;

	public Neo4jOrganizationExporter(Session session) {
		this.session = session;
	}

	/**
	 * Reads all manufacturers from the database and returns them as a stream of {@link Organization Organizations}.
	 */
	public Stream<Organization> loadOrganizations() {

		Result result = session.run(new Query(
				"MATCH (s:" + COMPANY_LABEL + ") " +
						"OPTIONAL MATCH (s)-[:" + COMPANY_HAS_ADDRESS_LABEL + "]-(a:" + ADDRESS_LABEL + ") " +
						"RETURN s.mmiId, s.name, s.shortName, collect({" +
						"street:a.street," +
						"streetNumber:a.streetNumber," +
						"postalCode:a.postalCode," +
						"city:a.city," +
						"country:a.country," +
						"countryCode:a.countryCode" +
						"}) as addresses"
		));
		return null;
	}

	private static Organization toOrganization(Record record) {
		Neo4jExportOrganisation exportOrganisation = new Neo4jExportOrganisation(record);

		Organization organization = new Organization();
		organization.active = true;
		Address address = new Address();
		address.setUse(Address.Use.WORK);

		// TODO
		return null;
	}

	private static class Neo4jExportOrganisation {
		final long mmiId;
		final String name;
		final String shortName;
		final List<Neo4jExportAddress> addresses;

		Neo4jExportOrganisation(MapAccessorWithDefaultValue value) {
			mmiId = value.get("mmiId").asLong();
			name = value.get("name", (String) null);
			shortName = value.get("shortName", (String) null);
			addresses = value.get("addresses").asList(Neo4jExportAddress::new);
		}
	}

	private static class Neo4jExportAddress {
		final String street;
		final String streetNumber;
		final String postalCode;
		final String city;
		final String country;
		final String countryCode;

		public Neo4jExportAddress(MapAccessorWithDefaultValue value) {
			street = value.get("street", (String) null);
			streetNumber = value.get("streetNumber", (String) null);
			postalCode = value.get("postalCode", (String) null);
			city = value.get("city", (String) null);
			country = value.get("country", (String) null);
			countryCode = value.get("countryCode", (String) null);
		}

	}

}
