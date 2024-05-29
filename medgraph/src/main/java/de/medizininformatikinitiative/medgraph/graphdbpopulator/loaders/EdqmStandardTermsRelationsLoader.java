package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.common.EDQM;
import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;
import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.EDQM_LABEL;

/**
 * Loader which reads pdf_relations.csv and assigns the relations between EDQM PDF Standard Terms and their
 * characteristics. Requires the EDQM Standard Term nodes to already exist.
 *
 * @author Markus Budeus
 */
public class EdqmStandardTermsRelationsLoader extends CsvLoader {

	private static final String SOURCE_CODE = "SOURCECODE";
	private static final String TARGET_CLASS = "TARGETCLASS";
	private static final String TARGET_CODE = "TARGETCODE";

	public EdqmStandardTermsRelationsLoader(Session session) {
		super(Path.of("pdf_relations.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(withLoadStatement(
				"MATCH (pdf:" + EDQM_LABEL + " {code: '" + EDQM.PHARMACEUTICAL_DOSE_FORM.getCodePrefix() + "'+" + row(
						SOURCE_CODE) + "}) " +
						"MATCH (ch:" + EDQM_LABEL + " {code: " + row(TARGET_CLASS) + "+'-'+" + row(
						TARGET_CODE) + "}) " +
						"CREATE (pdf)-[:" + EDQM_HAS_CHARACTERISTIC_LABEL + "]->(ch)"
		));
	}
}
