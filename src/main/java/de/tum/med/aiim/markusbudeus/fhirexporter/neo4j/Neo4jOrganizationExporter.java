package de.tum.med.aiim.markusbudeus.fhirexporter.neo4j;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Identifier;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.organization.Address;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.organization.Organization;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.util.List;
import java.util.stream.Stream;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public class Neo4jOrganizationExporter extends Neo4jExporter<Organization> {

	public Neo4jOrganizationExporter(Session session) {
		super(session);
	}

	/**
	 * Reads all manufacturers from the database and returns them as a stream of {@link Organization Organizations}.
	 */
	@Override
	public Stream<Organization> exportObjects() {
		Result result = session.run(new Query(
				"MATCH (s:" + COMPANY_LABEL + "{mmiId: 16227}) " +
						"OPTIONAL MATCH (s)-[:" + COMPANY_HAS_ADDRESS_LABEL + "]-(a:" + ADDRESS_LABEL + ") " +
						"RETURN s.mmiId AS mmiId, s.name AS name, s.shortName AS shortName, collect({" +
						"street:a.street," +
						"streetNumber:a.streetNumber," +
						"postalCode:a.postalCode," +
						"city:a.city," +
						"country:a.country," +
						"countryCode:a.countryCode" +
						"}) as addresses"
		));
		return result.stream().map(Neo4jOrganizationExporter::toOrganization);
	}

	private static Organization toOrganization(Record record) {
		Neo4jExportOrganisation exportOrganisation = new Neo4jExportOrganisation(record);

		Organization organization = new Organization();
		organization.active = true;
		organization.name = exportOrganisation.name;
		organization.identifier = Identifier.fromOrganizationMmiId(exportOrganisation.mmiId);
		if (exportOrganisation.shortName != null && !exportOrganisation.shortName.equals(exportOrganisation.name)) {
			organization.alias = new String[]{exportOrganisation.shortName};
		}

		organization.address = 		exportOrganisation.addresses
				.stream()
				.map(Neo4jExportAddress::toCompanyAddress)
				.toList()
				.toArray(new Address[0]);

		return organization;
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

		public Address toCompanyAddress() {
			Address address = new Address();
			address.setUse(Address.Use.WORK);

			String line = null;
			if (street != null) {
				if (streetNumber != null) {
					line = street + " " + streetNumber;
				} else {
					line = street;
				}
			}

			address.setAddress(new String[] { line }, postalCode, city, country);
			return address;
		}

	}

}
