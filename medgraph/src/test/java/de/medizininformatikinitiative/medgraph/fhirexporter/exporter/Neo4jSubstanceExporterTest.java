package de.medizininformatikinitiative.medgraph.fhirexporter.exporter;

import de.medizininformatikinitiative.medgraph.Neo4jTest;
import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphAddress;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphOrganization;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphSubstance;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Markus Budeus
 */
public class Neo4jSubstanceExporterTest extends Neo4jTest {

	private Neo4jSubstanceExporter sut;

	@BeforeEach
	void setUp() {
		sut = new Neo4jSubstanceExporter(session);
	}

	@Test
	void export() {
		// TODO
		Set<GraphSubstance> substances = sut.exportObjects().collect(Collectors.toSet());

		Set<GraphSubstance> expected = Set.of(
		);

	}
}
