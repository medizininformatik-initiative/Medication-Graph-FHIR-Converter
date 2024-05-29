package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Creates EDQM standard terms nodes using the edqm_objects.csv file.
 *
 * @author Markus Budeus
 */
public class EdqmStandardTermsLoader extends CsvLoader {

	public static final String EDQM_PDF_CLASS = "PDF";
	public static final String EDQM_PDF_TYPE = "Pharmaceutical dose form";
	public static final String EDQM_BDF_CLASS = "BDF";
	public static final String EDQM_BDF_TYPE = "Basic dose form";
	public static final String EDQM_ISI_CLASS = "ISI";
	public static final String EDQM_ISI_TYPE = "Intended site";
	public static final String EDQM_RCA_CLASS = "RCA";
	public static final String EDQM_RCA_TYPE = "Release characteristic";

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
				"code", EDQM_PDF_CLASS, "typeName", EDQM_PDF_TYPE);
		assignTyping(EDQM_BDF_CLASS, EDQM_BDF_TYPE);
		assignTyping(EDQM_ISI_CLASS, EDQM_ISI_TYPE);
		assignTyping(EDQM_RCA_CLASS, EDQM_RCA_TYPE);
	}

	private void assignTyping(String typeCode, String typeName) {
		executeQuery("MATCH (e:" + EDQM_LABEL + ") WHERE e.code STARTS WITH $code SET e.type = $typeName",
				"code", typeCode, "typeName", typeName);
	}

}
