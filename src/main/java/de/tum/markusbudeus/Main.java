package de.tum.markusbudeus;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import static org.neo4j.driver.Values.parameters;

public class Main {

	public static void main(String[] args) {
		try (DatabaseConnection connection = new DatabaseConnection();
		     Session session = connection.createSession()) {
			session.executeWrite(tx -> {
				var query = new Query("CREATE (m:SUBSTANCE {name: $name, mmi_id: $mmi_id})", parameters("name", "Adrenaline", "mmi_id", 1));
				var result = tx.run(query);
				return "";
			});
		}
	}
}