package de.tum.med.aiim.markusbudeus.graphdbpopulator.migrators;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions;
import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static org.neo4j.driver.Values.parameters;

/**
 * This class creates the Product nodes in the database using the PRODUCT table from the MMI PharmIndex.
 */
@Deprecated
public class ProductMigrator extends Migrator {

	private static final int ID_INDEX = 0;
	private static final int NAME_INDEX = 2;

	public ProductMigrator(Path directory, Session session) throws IOException {
		super(directory, "PRODUCT.CSV", session);
	}

	@Override
	public void migrateLine(String[] line) {
		addNode(Integer.parseInt(line[ID_INDEX]), line[NAME_INDEX]);
	}

	void addNode(int id, String name) {
		session.run(new Query(
				"CREATE (d:" + DatabaseDefinitions.PRODUCT_LABEL + " {name: $name, mmi_id: $mmi_id})",
				parameters("name", name, "mmi_id", id)
		)).consume();
	}

}
