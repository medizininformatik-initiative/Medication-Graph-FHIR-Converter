package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.common.EDQM;
import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.EDQM.*;
import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Creates EDQM standard terms nodes using the edqm_objects.csv file.
 *
 * @author Markus Budeus
 */
public class EdqmStandardTermsLoader extends CsvLoader {

	private static final String CLASS = "CLASS";
	private static final String CODE_IN_CLASS = "CODE";
	private static final String NAME = "NAME";
	private static final String STATUS = "STATUS";

	public EdqmStandardTermsLoader(Session session) {
		super(Path.of("edqm_objects.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT edqmCodeConstraint IF NOT EXISTS FOR (e:" + EDQM_LABEL + ") REQUIRE e.code IS UNIQUE"
		);
		executeQuery(
				"CREATE CONSTRAINT edqmPdfNameConstraint IF NOT EXISTS FOR (e:" + EDQM_PDF_LABEL + ") REQUIRE e.name IS UNIQUE"
		);

		startSubtask("Creating Standard Terms nodes");
		executeQuery(withLoadStatement(
				"CREATE (e:" + EDQM_LABEL + ":" + CODE_LABEL + " {" +
						"code: " + row(CLASS) + " + '-' + " + row(CODE_IN_CLASS) +
						", name: " + row(NAME) + ", status: " + row(STATUS) + "})"
		));

		startSubtask("Assigning Standard Terms types");
		executeQuery(
				"MATCH (e:" + EDQM_LABEL + ") WHERE e.code STARTS WITH $code " +
						"SET e:" + EDQM_PDF_LABEL + ", e.type = $typeName",
				"code", PHARMACEUTICAL_DOSE_FORM.getShorthand(), "typeName", PHARMACEUTICAL_DOSE_FORM.getTypeFullName());
		assignTyping(BASIC_DOSE_FORM);
		assignTyping(INTENDED_SITE);
		assignTyping(RELEASE_CHARACTERISTIC);
	}

	private void assignTyping(EDQM edqm) {
		executeQuery("MATCH (e:" + EDQM_LABEL + ") WHERE e.code STARTS WITH $code SET e.type = $typeName",
				"code", edqm.getShorthand(), "typeName", edqm.getTypeFullName());
	}

}
