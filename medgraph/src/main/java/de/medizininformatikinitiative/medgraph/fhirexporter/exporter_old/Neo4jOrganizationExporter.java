package de.medizininformatikinitiative.medgraph.fhirexporter.exporter_old;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Meta;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.FhirAddress;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.Organization;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.util.List;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * This class generates {@link Organization}-objects using the Neo4j knowledge graph.
 *
 * @author Markus Budeus
 */
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
				"MATCH (s:" + COMPANY_LABEL + ") " +
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

	@Override
	protected String createObjectCountQuery() {
		return "MATCH (o:"+COMPANY_LABEL+") RETURN COUNT(o)";
	}

	private static Organization toOrganization(Record record) {
		Neo4jExportOrganisation exportOrganisation = new Neo4jExportOrganisation(record);

		Organization organization = new Organization();
		organization.meta = new Meta();
		organization.meta.profile = new String[] { "http://hl7.org/fhir/StructureDefinition/Organization" };
		organization.meta.source = META_SOURCE;
		organization.active = true;
		organization.name = exportOrganisation.name;
		organization.identifier = new Identifier[] { Identifier.fromOrganizationMmiId(exportOrganisation.mmiId) };
		if (exportOrganisation.shortName != null && !exportOrganisation.shortName.equals(exportOrganisation.name)) {
			organization.alias = new String[]{exportOrganisation.shortName};
		}

		organization.fhirAddresses = 		exportOrganisation.addresses
				.stream()
				.map(Neo4jExportAddress::toCompanyAddress)
				.toList()
				.toArray(new FhirAddress[0]);

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

		public FhirAddress toCompanyAddress() {
			FhirAddress fhirAddress = new FhirAddress();
			fhirAddress.setUse(FhirAddress.Use.WORK);

			String line = null;
			if (street != null) {
				if (streetNumber != null) {
					line = street + " " + streetNumber;
				} else {
					line = street;
				}
			}

			fhirAddress.setAddress(new String[] { line }, postalCode, city, country);
			return fhirAddress;
		}

	}

}
