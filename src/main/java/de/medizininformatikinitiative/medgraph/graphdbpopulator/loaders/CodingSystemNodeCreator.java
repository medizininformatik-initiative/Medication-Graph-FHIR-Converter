package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.CodingSystem;
import org.neo4j.driver.Session;

import java.time.LocalDate;

import static de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions.*;

/**
 * This loader creates the Coding System nodes and connects all corresponding code nodes to it.
 *
 * @author Markus Budeus
 */
public class CodingSystemNodeCreator extends Loader {

	public CodingSystemNodeCreator(Session session) {
		super(session);
	}

	@Override
	protected void executeLoad() {
		for (CodingSystem codingSystem : CodingSystem.values()) {
			createCodingSystemNodeAndConnectAllLabelledNodes(
					codingSystem.uri,
					codingSystem.name,
					codingSystem.dateOfRetrieval,
					codingSystem.notice,
					codingSystem.assignedNodesLabel
			);
		}
	}

	/**
	 * Returns a Cypher create statement for a coding system node. The properties may be null.
	 *
	 * @param uri    the uri property
	 * @param name   the name property
	 * @param date   the date property
	 * @param notice the notice property
	 * @return the cypher statement to create said node
	 */
	private String createCodingSytemNode(String uri, String name, LocalDate date, String notice) {
		return "CREATE (" + "cs" + ":" + CODING_SYSTEM_LABEL + " {" +
				"uri: " + quoteOrNull(uri) + ", " +
				"name: " + quoteOrNull(name) + ", " +
				"date: " + toCypherDate(date) + ", " +
				"notice: " + quoteOrNull(notice) +
				"}) ";
	}

	/**
	 * Executes a Cypher statement to create a coding system node as well as connect all nodes with the specified label
	 * to it. The properties may be null.
	 *
	 * @param uri    the uri property
	 * @param name   the name property
	 * @param date   the date property
	 * @param notice the notice property
	 * @param label  the label for which to connect all nodes with this label to the new coding system node
	 */
	private void createCodingSystemNodeAndConnectAllLabelledNodes(String uri, String name,
	                                                              LocalDate date, String notice, String label) {
		executeQuery(createCodingSytemNode(uri, name, date, notice));
		executeQuery(
				"MATCH (cs:" + CODING_SYSTEM_LABEL + " {uri: $uri}) " +
						"MATCH (e:" + label + ") " +
						withRowLimit("WITH cs, e " +
								"CREATE (e)-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(cs)"),
				"uri", uri
		);
	}

}
