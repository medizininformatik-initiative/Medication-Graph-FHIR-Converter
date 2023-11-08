package de.tum.med.aiim.markusbudeus.fhirexporter.neo4j;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.CodeableConcept;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Coding;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Identifier;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.organization.Organization;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance.Substance;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

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
		return null;
	}

	private static Organization toOrganization(Record record) {
		return null;
	}

}
