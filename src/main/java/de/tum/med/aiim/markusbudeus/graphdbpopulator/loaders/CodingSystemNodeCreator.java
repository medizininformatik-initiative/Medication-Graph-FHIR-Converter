package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.time.LocalDate;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

/**
 * This loader creates the Coding System nodes and connects all corresponding code nodes to it.
 */
public class CodingSystemNodeCreator extends Loader {

	public CodingSystemNodeCreator(Session session) {
		super(session);
	}

	@Override
	protected void executeLoad() {
		createCodingSystemNodeAndConnectAllLabelledNodes(
				"standardterms.edqm.eu",
				"EDQM Standard Terms database",
				LocalDate.of(2023, 10, 16),
				"Data is taken from the EDQM Standard Terms database and is reproduced with permission " +
						"of the European Directorate for the Quality of Medicines & HealthCare, Council of Europe (EDQM). " +
						"The data has been retrieved at the date given by the date property. Since the EDQM Standard " +
						"Terms database is not a static list, this data may not be up to date.",
				EDQM_LABEL
		);

		createCodingSystemNodeAndConnectAllLabelledNodes(
				"http://fhir.de/CodeSystem/bfarm/atc",
				"Anatomisch-Therapeutisch-Chemische Klassifikation",
				LocalDate.now(),
				"Data of the ATC classification has been retrieved from the MMI PharmIndex raw data on the " +
						"date specified by this node. Be aware that the raw data provided to create this database " +
						"may already have been outdated when this database was created. As such, the data may be " +
						"older than what the date suggests.",
				ATC_LABEL
		);

		createCodingSystemNodeAndConnectAllLabelledNodes(
				"http://fhir.de/CodeSystem/ifa/pzn",
				"Pharmazentralnummer",
				LocalDate.now(),
				"The PZN data has been retrieved from raw MMI PharmIndex data when this database was created. " +
						"The database creation date is specified as date property on this node. However, the raw " +
						"data used to instantiate this database may have been older than that.",
				PZN_LABEL
		);

		createCodingSystemNodeAndConnectAllLabelledNodes(
				"http://fhir.de/CodeSystem/ask",
				"Arzneistoffkatalog",
				LocalDate.now(),
				"The ASK data has been retrieved from raw MMI PharmIndex data when this database was created. " +
						"The database creation date is specified as date property on this node. However, the raw " +
						"data used to instantiate this database may have been older than that.",
				ASK_LABEL
		);

		createCodingSystemNodeAndConnectAllLabelledNodes(
				"https://www.who.int/teams/health-product-and-policy-standards/inn",
				"International Nonproprietary Name",
				LocalDate.of(2023, 8, 28),
				"The INN data has been retrieved from " +
						"https://www.wcoomd.org/en/topics/nomenclature/instrument-and-tools/tools-to-assist-with-the-classification-in-the-hs/hs_classification-decisions/inn-table.aspx " +
						"on the specified date.",
				INN_LABEL
		);
	}

	/**
	 * Returns a Cypher create statement for a coding system node. The properties may be null.
	 *
	 * @param uri    the uri property
	 * @param name   the name property
	 * @param date   the date property
	 * @param notice the notice property
	 * @return the cypher statement to create said node
	 */
	private String createCodingSytemNode(String uri, String name, LocalDate date, String notice) {
		return "CREATE (" + "cs" + ":" + CODING_SYSTEM_LABEL + " {" +
				"uri: " + quoteOrNull(uri) + ", " +
				"name: " + quoteOrNull(name) + ", " +
				"date: " + toCypherDate(date) + ", " +
				"notice: " + quoteOrNull(notice) +
				"}) ";
	}

	/**
	 * Executes a Cypher statement to create a coding system node as well as connect all nodes with the specified label
	 * to it. The properties may be null.
	 *
	 * @param uri    the uri property
	 * @param name   the name property
	 * @param date   the date property
	 * @param notice the notice property
	 * @param label  the label for which to connect all nodes with this label to the new coding system node
	 */
	private void createCodingSystemNodeAndConnectAllLabelledNodes(String uri, String name,
	                                                              LocalDate date, String notice, String label) {
		executeQuery(createCodingSytemNode(uri, name, date, notice) +
				"WITH cs " +
				"MATCH (e:" + label + ") " +
				"CREATE (e)-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(cs)"
		);
	}

}
