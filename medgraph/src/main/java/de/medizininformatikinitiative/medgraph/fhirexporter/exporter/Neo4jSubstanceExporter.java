package de.medizininformatikinitiative.medgraph.fhirexporter.exporter;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphSubstance;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphUtil;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * This class generates {@link GraphSubstance}-objects using the Neo4j knowledge graph.
 *
 * @author Markus Budeus
 */
public class Neo4jSubstanceExporter extends Neo4jExporter<GraphSubstance> {

	/**
	 * Instantiates a new exporter.
	 *
	 * @param session the database session to use for querying the knowledge graph
	 */
	public Neo4jSubstanceExporter(Session session) {
		super(session);
	}

	/**
	 * Reads all substances with their assigned codes and coding systems from the database and returns them as a stream
	 * of {@link GraphSubstance Substances}.
	 */
	@Override
	public Stream<GraphSubstance> exportObjects() {
		Result result = session.run(new Query(
				"MATCH (s:" + SUBSTANCE_LABEL + ") " +
						"OPTIONAL MATCH (cs:" + CODING_SYSTEM_LABEL + ")<-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]-(c:" + CODE_LABEL + ")-->(s) " +
						"WITH s, collect(" + GraphUtil.groupCodingSystem("c", "cs") + ") AS codes " +
						"RETURN s.mmiId, s.name, codes"
		));

		return result.stream().map(GraphSubstance::new);
	}

}
