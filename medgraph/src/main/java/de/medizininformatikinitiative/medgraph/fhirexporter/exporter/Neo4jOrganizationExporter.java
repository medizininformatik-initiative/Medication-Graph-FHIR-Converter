package de.medizininformatikinitiative.medgraph.fhirexporter.exporter;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphAddress;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphOrganization;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;
import static de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphOrganization.*;

/**
 * This class generates {@link GraphOrganization}-objects using the Neo4j knowledge graph.
 *
 * @author Markus Budeus
 */
public class Neo4jOrganizationExporter extends Neo4jExporter<GraphOrganization> {

	public Neo4jOrganizationExporter(Session session) {
		super(session);
	}

	/**
	 * Reads all manufacturers from the database and returns them as a stream of {@link Organization Organizations}.
	 */
	@Override
	public Stream<GraphOrganization> exportObjects() {
		Result result = session.run(new Query(
				"MATCH (s:" + COMPANY_LABEL + ") " +
						"OPTIONAL MATCH (s)-[:" + COMPANY_HAS_ADDRESS_LABEL + "]-(a:" + ADDRESS_LABEL + ") " +
						"RETURN s.mmiId AS " + MMI_ID + ", s.name AS " + NAME + ", s.shortName AS " + SHORT_NAME + ", " +
						"collect({" +
						GraphAddress.STREET + ":a.street," +
						GraphAddress.STREET_NUMBER + ":a.streetNumber," +
						GraphAddress.POSTAL_CODE + ":a.postalCode," +
						GraphAddress.CITY + ":a.city," +
						GraphAddress.COUNTRY + ":a.country," +
						GraphAddress.COUNTRY_CODE + ":a.countryCode" +
						"}) as " + ADDRESSES
		));
		return result.stream().map(GraphOrganization::new);
	}

}
