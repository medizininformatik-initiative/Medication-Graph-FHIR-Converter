package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * This loader uses the COMPANYADDRESS.CSV to create Address nodes and link them to the company nodes. Requires company
 * nodes to already exist.
 *
 * @author Markus Budeus
 */
public class CompanyAddressLoader extends CsvLoader {

	private static final String MMI_ID = "ID";
	private static final String COMPANY_ID = "COMPANYID";
	private static final String COUNTRY_CATALOG_CODE = "COUNTRYCODE";
	private static final String STREET = "STREET";
	private static final String STREET_NUMBER = "STREETNUMBER";
	private static final String ZIP_CODE = "ZIP";
	private static final String CITY = "CITY";
	private static final String ADDRESS_TYPE_CODE = "ADDRESSTYPECODE";

	private final CompanyAddressCountryLoader countryLoader;

	public CompanyAddressLoader(Session session) {
		super("COMPANYADDRESS.CSV", session);
		countryLoader = new CompanyAddressCountryLoader(session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT addressMmiIdConstraint IF NOT EXISTS FOR (a:" + ADDRESS_LABEL + ") REQUIRE a.mmiId IS UNIQUE"
		);
		startSubtask("Loading CSV file");
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + row(
						ADDRESS_TYPE_CODE) + " = 'C' " + // Only allow 'Firmensitz' Addresses
						"MATCH (c:" + COMPANY_LABEL + " {mmiId: " + intRow(COMPANY_ID) + "}) " +
						"CREATE (c)-[:" + COMPANY_HAS_ADDRESS_LABEL + "]->(a:" + ADDRESS_LABEL + " {" +
						"mmiId: " + intRow(MMI_ID) + ", " +
						"countryCode: " + nullIfBlank(row(COUNTRY_CATALOG_CODE)) + ", " +
						"street: " + nullIfBlank(row(STREET)) + ", " +
						"streetNumber: " + nullIfBlank(row(STREET_NUMBER)) + ", " +
						"postalCode: " + nullIfBlank(row(ZIP_CODE)) + ", " +
						"city: " + nullIfBlank(row(CITY)) +
						"})"
		));

		startSubtask("Removing empty entries");
		executeQuery("MATCH (a:Address) WHERE size(keys(a)) = 1 DETACH DELETE a");

		startSubtask("Assigning correct country codes");
		// Replace MMI country code by values from catalog
		countryLoader.executeLoad();
	}

	private static class CompanyAddressCountryLoader extends CatalogEntryLoader {

		private static final int COUNTRY_CODE_CATALOG_ID = 101;

		public CompanyAddressCountryLoader(Session session) {
			super(session);
		}

		@Override
		protected void executeLoad() {
			executeQuery(
					"CREATE CONSTRAINT tempCountryCodes IF NOT EXISTS FOR (d:CountryCode) REQUIRE d.mmiCode IS UNIQUE"
			);
			executeQuery(withFilteredLoadStatement(COUNTRY_CODE_CATALOG_ID,
					"CREATE (d:CountryCode {mmiCode: " + row(CODE) + ", " +
							"country: " + row(NAME) + ", " +
							"countryCode: " + row(SHORT_NAME) + "})"
			));
			executeQuery(
					"MATCH (a:" + ADDRESS_LABEL + ") " +
							"MATCH (c:CountryCode {mmiCode: a.countryCode}) " +
							withRowLimit("WITH a, c SET a.country = c.country, a.countryCode = c.countryCode")
			);
			executeQuery("MATCH (c:CountryCode) DELETE c");
			executeQuery("DROP CONSTRAINT tempCountryCodes");
		}
	}
}
