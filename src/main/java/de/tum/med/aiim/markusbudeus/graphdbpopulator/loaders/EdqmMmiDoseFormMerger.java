package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;
import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.DOSE_FORM_LABEL;

public class EdqmMmiDoseFormMerger extends Loader {

	public EdqmMmiDoseFormMerger(Session session) {
		super(session);
	}

	@Override
	protected void executeLoad() {
		session.run(
				"MATCH (d:" + DOSE_FORM_LABEL + ")-[:" + DOSE_FORM_IS_EDQM + "]->(e:" + EDQM_LABEL + ")-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(cs:" + CODING_SYSTEM_LABEL + ") " +
						"SET d.edqmCode = e.code " +
						"SET d.edqmName = e.name " +
						"SET d.edqmStatus = e.status " +
						"SET d.edqmIntendedSite = e.intendedSite " +
						"SET d:" + EDQM_LABEL + " " +
						"SET d:" + CODING_SYSTEM_LABEL + " " +
						"CREATE (d)-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(cs)"
		);
		session.run(
				"MATCH (e:" + EDQM_LABEL + ") WHERE NOT e:" + DOSE_FORM_LABEL + " DETACH DELETE e"
		);
	}
}
