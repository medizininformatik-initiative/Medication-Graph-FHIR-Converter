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

	public static void runMigrators(Path baseDir) throws IOException {
		try (DatabaseConnection connection = new DatabaseConnection();
		     Session session = connection.createSession()) {

			List<Migrator> migrators = new ArrayList<>();

			migrators.add(new SubstanceMigrator(baseDir, session));
			migrators.add(new ProductMigrator(baseDir, session));
			migrators.add(new InnMigrator(session));
			migrators.add(new CompanyMigrator(baseDir, session));
			migrators.add(new CompanyDrugReferenceMigrator(baseDir, session));
			migrators.add(new DrugMigrator(baseDir, session));
			migrators.add(new IngredientMigrator(baseDir, session));

			for (Migrator migrator : migrators) {
				migrator.migrate();
			}

		}
	}

}