package de.medizininformatikinitiative.medgraph.fhirexporter.exporter;

import de.medizininformatikinitiative.medgraph.Neo4jTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphAddress;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphOrganization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Markus Budeus
 */
public class Neo4jOrganizationExporterTest extends Neo4jTest {

	private Neo4jOrganizationExporter sut;

	@BeforeEach
	void setUp() {
		sut = new Neo4jOrganizationExporter(session);
	}

	@Test
	void export() {
		Set<GraphOrganization> organizations = sut.exportObjects().collect(Collectors.toSet());

		Set<GraphOrganization> expected = Set.of(
				new GraphOrganization(						1, "Bayer Vital GmbH", "Bayer Vital", List.of(
						new GraphAddress("Kaiser-Wilhelm-Allee", "56", "51368", "Leverkusen", "Deutschland", "DE")
				)),
				new GraphOrganization(2, "Dr. Schumacher GmbH", "Dr. Schumacher GmbH", List.of(
						new GraphAddress("Zum Steger", "3", "34323", "Malsfeld", "Deutschland", "DE")
				))
		);

		assertEquals(expected, organizations);
	}
}
