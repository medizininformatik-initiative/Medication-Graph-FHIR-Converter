package de.medizininformatikinitiative.medgraph.searchengine.tools;

import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

/**
 * Class which provides some useful database actions.
 *
 * @author Markus Budeus
 */
public class DatabaseTools {

	/**
	 * Using the given session, drops all relationships, nodes and constraints from the database.
	 * @param session the session to access the database with
	 */
	public static void clearDatabase(Session session) {
		// Drop relationships
		session.run("MATCH ()-[r]->() CALL { WITH r DELETE r } IN TRANSACTIONS OF 10000 ROWS");
		// Drop nodes
		session.run("MATCH (n) CALL { WITH n DETACH DELETE n } IN TRANSACTIONS OF 10000 ROWS");
		// Load constraints
		Result constraints = session.run("SHOW CONSTRAINTS YIELD name");
		List<String> constraintNames = new ArrayList<>();
		constraints.forEachRemaining(record -> constraintNames.add(record.get("name", (String) null)));
		// Drop constraints
		constraintNames.forEach(name -> {
			session.run(new Query("DROP CONSTRAINT $constraint", parameters("constraint", name)));
		});
	}
}
