package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

/**
 * This loader uses the COMPANYADDRESS.CSV to create Address nodes and link them to the company nodes. Requires company
 * nodes to already exist.
 */
public class CompanyAddressLoader extends CsvLoader {

	private static final String COMPANY_ID = "COMPANYID";
	private static final String COUNTRY_CATALOG_CODE = "COUNTRYCODE";
	private static final String STREET = "STREET";
	private static final String STREET_NUMBER = "STREETNUMBER";
	private static final String ZIP_CODE = "ZIP";
	private static final String CITY = "CITY";
	private static final String ADDRESS_TYPE_CODE = "ADDRESSTYPECODE";

	private final CompanyAddressCountryLoader countryLoader;

	public CompanyAddressLoader(Session session) throws IOException {
		super("COMPANYADDRESS.CSV", session);
		countryLoader = new CompanyAddressCountryLoader(session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + row(
						ADDRESS_TYPE_CODE) + " = 'C' " + // Only allow 'Firmensitz' Addresses
						"MATCH (c:" + COMPANY_LABEL + " {mmiId: " + intRow(COMPANY_ID) + "}) " +
						"CREATE (c)-[:" + COMPANY_HAS_ADDRESS_LABEL + "]->(a:" + ADDRESS_LABEL + " {" +
						"countryCode: " + row(COUNTRY_CATALOG_CODE) + ", " +
						"street: " + row(STREET) + ", " +
						"streetNumber: " + row(STREET_NUMBER) + ", " +
						"postalCode: " + row(ZIP_CODE) + ", " +
						"city: " + row(CITY) +
						"})"
		));

		// Replace MMI country code by values from catalog
		countryLoader.executeLoad();
	}

	private static class CompanyAddressCountryLoader extends CatalogEntryLoader {

		private static final int COUNTRY_CODE_CATALOG_ID = 101;

		public CompanyAddressCountryLoader(Session session) throws IOException {
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
							" SET a.country = c.country, a.countryCode = c.countryCode"
			);
			executeQuery("MATCH (c:CountryCode) DELETE c");
			executeQuery("DROP CONSTRAINT tempCountryCodes");
		}
	}
}
