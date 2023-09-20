package de.tum.med.aiim.markusbudeus.graphdbpopulator.migrators;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.CSVReader;
import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import org.junit.jupiter.api.Test;

class SubstanceMigratorExecutor {

	@Test
	public void runExample() {
		DatabaseConnection.runSession(session -> {
			SubstanceMigrator migrator = new SubstanceMigrator((CSVReader) null, session);

			migrator.addNode("Adrenaline", 1, "ASK1", "CAS1");
			migrator.addNode("Morphine", 2, "ASK1", "CAS2");
			migrator.addNode("Flumazenil", 3, "ASK2", "CAS1");
			migrator.addNode("Pipamperone", 4, "ASK3", "CAS3");
			migrator.addNode("Adenosine", 5, "ASK1", "CAS1");
		});
	}

}