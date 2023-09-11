package de.tum.markusbudeus;

import de.tum.markusbudeus.migrators.*;
import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class Main {

	public static void main(String[] args) {
		try (DatabaseConnection connection = new DatabaseConnection();
		     Session session = connection.createSession()) {
			session.executeWrite(tx -> {
				var query = new Query("CREATE (m:SUBSTANCE {name: $name, mmi_id: $mmi_id})",
						parameters("name", "Adrenaline", "mmi_id", 1));
				var result = tx.run(query);
				return "";
			});
		}
	}

	public static void runMigrators(Path baseDir, boolean includeInn) throws IOException, InterruptedException {
		try (DatabaseConnection connection = new DatabaseConnection();
		     Session session = connection.createSession()) {

			List<Migrator> migrators = new ArrayList<>();

			// Substance nodes, ASK nodes and CAS nodes and their relations
			migrators.add(new SubstanceMigrator(baseDir, session));
			// Product nodes
			migrators.add(new ProductMigrator(baseDir, session));
			// PZN nodes and their relations with Product nodes
			migrators.add(new PznMigrator(baseDir, session));
			if (includeInn) {
				// INN nodes and relations to CAS nodes
				migrators.add(new InnMigrator(session));
			}
			// Manufacturer nodes
			migrators.add(new CompanyMigrator(baseDir, session));
			// Relation between Manufacturer nodes and their product nodes
			migrators.add(new CompanyProductReferenceMigrator(baseDir, session));
			// Drug nodes and relations to Product nodes
			migrators.add(new DrugMigrator(baseDir, session));
			// Ingredient nodes and relations to Substance nodes
			migrators.add(new IngredientMigrator(baseDir, session));
			// Relations between Ingredient nodes and Drug nodes
			migrators.add(new DrugIngredientConnectionMigrator(baseDir, session));

			for (Migrator migrator : migrators) {
				migrator.migrate();
			}

			new UcumUnitCreator(session).createUnits();

		}
	}

}