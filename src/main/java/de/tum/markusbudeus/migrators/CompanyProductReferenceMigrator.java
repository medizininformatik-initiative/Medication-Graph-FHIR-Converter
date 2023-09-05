package de.tum.markusbudeus.migrators;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.tum.markusbudeus.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

/**
 * Uses the PRODUCT_COMPANY table from the MMI PharmIndex to create references between Product and Manufacturer nodes.
 * Requires the Product and Manufacturer nodes to already exist.
 */
public class CompanyProductReferenceMigrator extends Migrator {

	private static final int PRODUCT_ID_INDEX = 0;
	private static final int COMPANY_ID_INDEX = 1;

	public CompanyProductReferenceMigrator(Path directory, Session session)
	throws IOException {
		super(directory, "PRODUCT_COMPANY.CSV", session);
	}

	@Override
	public void migrateLine(String[] line) {
		createReference(
				Integer.parseInt(line[PRODUCT_ID_INDEX]),
				Integer.parseInt(line[COMPANY_ID_INDEX])
		);
	}

	private void createReference(int productId, int companyId) {
		session.run(new Query(
				"MATCH (c:" + COMPANY_LABEL + " {mmi_id: $company_id}) " +
						"MATCH (d:" + PRODUCT_LABEL + " {mmi_id: $drug_id}) " +
						"CREATE (c)-[r:" + MANUFACTURES_LABEL + "]->(d)",
				parameters("company_id", companyId, "drug_id", productId)
		)).consume();
	}
}
