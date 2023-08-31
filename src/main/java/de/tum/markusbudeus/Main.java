package de.tum.markusbudeus;

import de.tum.markusbudeus.migrators.*;
import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

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

			SubstanceMigrator substanceMigrator = new SubstanceMigrator(baseDir, session);
			ProductMigrator productMigrator = new ProductMigrator(baseDir, session);
			InnMigrator innMigrator = new InnMigrator(session);
			CompanyMigrator companyMigrator = new CompanyMigrator(baseDir, session);
			CompanyDrugReferenceMigrator companyDrugReferenceMigrator = new CompanyDrugReferenceMigrator(baseDir,
					session);

			substanceMigrator.migrate();
			productMigrator.migrate();
			innMigrator.migrate();
			companyMigrator.migrate();
			companyDrugReferenceMigrator.migrate();

		}
	}

}