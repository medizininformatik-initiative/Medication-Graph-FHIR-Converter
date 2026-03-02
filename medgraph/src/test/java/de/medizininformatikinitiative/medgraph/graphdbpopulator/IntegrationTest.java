package de.medizininformatikinitiative.medgraph.graphdbpopulator;

import de.medizininformatikinitiative.medgraph.DI;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionException;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionService;
import org.junit.jupiter.api.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.common.EDQM.BASIC_DOSE_FORM;
import static de.medizininformatikinitiative.medgraph.common.EDQM.PHARMACEUTICAL_DOSE_FORM;
import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs the whole migration on a set of sample files.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTest {

	// TODO Check Midazolam Legacy Correspondence. Resource files already include that.

	protected DatabaseConnection connection;
	protected Session session;

	protected void integrationTestSetup(boolean includeArchive) throws IOException {
		// The integration test must run against a "real" Neo4j environment!
		// The connection mechanism below only works if you have locally saved connection information (happens when you
		// connect via the regular application UI once.) Otherwise you must provide the connection information via
		// new DatabaseConnection(...)
		try {
			connection = DI.get(DatabaseConnectionService.class).createConnection();
		} catch (DatabaseConnectionException e) {
			throw new IllegalStateException(
					"Failed to create database connection for integration test! The integration test must run against a \"real\" Neo4j environment! " +
							"The default connection mechanism in this test only works if you have locally saved connection information (happens when you" +
							" connect via the regular application UI once.) Otherwise you must provide the connection information via" +
							" new DatabaseConnection(...)", e);
		}
		session = connection.createSession();

		DI.get(GraphDbPopulationFactory.class).prepareDatabasePopulation(
				  Path.of("src", "test", "resources", "sample"),
//				  Path.of("/var", "lib", "neo4j", "import"),
				  Path.of("/usr", "local", "neo4j", "import"),
				  Path.of("src", "test", "resources", "sample", "amice_stoffbez_synthetic.csv")
		  )
		  .executeDatabasePopulation(connection, includeArchive);
	}

	@AfterAll
	public void cleanup() {
		session.close();
		connection.close();
	}

	@Test
	public void midazolamAskCode() {
		Result result = session.run(
				"MATCH (a:" + ASK_LABEL + ")-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s:" + SUBSTANCE_LABEL + " {name: 'Midazolam'}) RETURN a.code");
		assertEquals("22661", result.next().get(0).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void midazolamCasCode() {
		Result result = session.run(
				"MATCH (a:" + CAS_LABEL + ")-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + " {primary: true}]->(s:" + SUBSTANCE_LABEL + " {name: 'Midazolam'}) RETURN a.code");
		assertEquals("59467-70-8", result.next().get(0).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void productsContainingMidazolamhydrochlorid() {
		Result result = session.run(
				"MATCH (p:" + PRODUCT_LABEL + ")-[c1:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->" +
						"(d:" + DRUG_LABEL + ")-[c2:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->" +
						"(i:" + MMI_INGREDIENT_LABEL + ")-[c3:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->" +
						"(s:" + SUBSTANCE_LABEL + " {name: 'Midazolamhydrochlorid'}) " +
						"WHERE NOT p.archived " +
						"RETURN s, p.name, i.massFrom"
		);

		Record r1 = result.next();
		Record r2 = result.next();

		assertFalse(result.hasNext());
		// Both products must point to the same midazolam node
		assertEquals(r1.get(0).asNode().elementId(), r2.get(0).asNode().elementId());

		checkProductMatchesIngredient(r1);
		checkProductMatchesIngredient(r2);
	}

	@Test
	public void manufacturerConnected() {
		Result result = session.run(
				"MATCH (m:" + COMPANY_LABEL + ")-[r:" + MANUFACTURES_LABEL + "]-(p:" + PRODUCT_LABEL + ")" +
						"WHERE NOT p.archived " +
						"RETURN p.mmiId, m.mmiId"
		);

		int[] manufacturerMmiId = new int[]{-1, -1, -1}; // Index is the product mmi id, value the manufacturer id

		int results = 0;
		while (result.hasNext()) {
			results++;
			Record record = result.next();
			int mmiId = record.get(0).asInt();
			int manufacturerId = record.get(1).asInt();
			if (manufacturerMmiId[mmiId] != -1) {
				// Note: Manufacturer 1 acts as distributor for product 2, but it should not be listed as manufacturer!
				fail("The product with mmi_id " + mmiId + " was included twice in the result!");
			}
			manufacturerMmiId[mmiId] = manufacturerId;
		}

		assertEquals(3, results);
		assertEquals(0, manufacturerMmiId[0]);
		assertEquals(1, manufacturerMmiId[1]);
		assertEquals(0, manufacturerMmiId[2]);
	}

	@Test
	public void belocDoseForm() {
		Result result = session.run(
				"MATCH (p:" + PRODUCT_LABEL + " {name: 'Beloc mite'})-[pd:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d:" + DRUG_LABEL + ")" +
						"-[df:" + DRUG_HAS_DOSE_FORM_LABEL + "]->(f:" + MMI_DOSE_FORM_LABEL + ")-[:" + DOSE_FORM_IS_EDQM + "]->(e:" + EDQM_LABEL + ") " +
						"RETURN e.code, e.name"
		);

		Record record = result.next();
		assertEquals("PDF-10219000", record.get(0).asString());
		assertEquals("Tablet", record.get(1).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void midazolamDoseForm() {
		Result result = session.run(
				"MATCH (p:" + PRODUCT_LABEL + " {name: 'Dormicum V 5 mg/5 ml'})-[pd:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d:" + DRUG_LABEL + ")" +
						"-[df:" + DRUG_HAS_DOSE_FORM_LABEL + "]->(f:" + MMI_DOSE_FORM_LABEL + ")-[:" + DOSE_FORM_IS_EDQM + "]->(e:" + EDQM_LABEL + ") " +
						"RETURN e.code, e.name"
		);

		Record record = result.next();
		assertEquals("PDF-11201000", record.get(0).asString());
		assertEquals("Solution for injection", record.get(1).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void midazolamDrugAmount() {
		Result result = session.run(
				"MATCH (p:" + PRODUCT_LABEL + " {name: 'Dormicum 15 mg/3 ml'})-[pd:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d:" + DRUG_LABEL + ")" +
						"-[du:" + DRUG_HAS_UNIT_LABEL + "]->(u:" + UNIT_LABEL + ") " +
						"RETURN d.amount, u.ucumCs"
		);

		Record record = result.next();
		assertEquals("3", record.get(0).asString());
		assertEquals("ml", record.get(1).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void midazolamUniiAndRxcui() {
		Result result = session.run(
				"MATCH (s:" + SUBSTANCE_LABEL + " {name: 'Midazolam'}) " +
						"MATCH (s)--(c:" + UNII_LABEL + ") " +
						"MATCH (s)--(r:" + RXCUI_LABEL + ") " +
						"RETURN c.code, r.code"
		);

		Record record = result.next();
		assertEquals("R60L0SM5BC", record.get(0).asString());
		assertEquals("6960", record.get(1).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void atcHierarchy() {
		Result result = session.run(
				"MATCH (a:" + ATC_LABEL + ")-[:" + ATC_HAS_PARENT_LABEL + "]->(p:" + ATC_LABEL + " {code: 'A01AA'}) " +
						"RETURN a.code, p.code"
		);

		Set<String> childCodes = new HashSet<>(2);

		for (int i = 0; i < 2; i++) {
			Record record = result.next();
			childCodes.add(record.get(0).asString());
			assertEquals("A01AA", record.get(1).asString());
		}
		assertFalse(result.hasNext());
		assertEquals(Set.of("A01AA01", "A01AA02"), childCodes);
	}

	@Test
	public void belocAtc() {
		Result result = session.run(
				"MATCH (m:" + PRODUCT_LABEL + " {name: 'Beloc mite'})--(:" + DRUG_LABEL + ")" +
						"-[:" + DRUG_MATCHES_ATC_CODE_LABEL + "]->(a:" + ATC_LABEL + ") " +
						"RETURN a.code"
		);

		Record record = result.next();
		assertEquals("A01AA02", record.get(0).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void midazolamhydrochloridCorrespondingIngredient() {
		Result result = session.run(
				"MATCH (p:" + PRODUCT_LABEL + " {name: 'Dormicum 15 mg/3 ml'})-->(d:" + DRUG_LABEL + ")" +
						"-->(i:" + MMI_INGREDIENT_LABEL + ")-[:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->" +
						"(ci:" + INGREDIENT_LABEL + ")--(s:" + SUBSTANCE_LABEL + ") " +
						"MATCH (ci)--(u:" + UNIT_LABEL + ") " +
						"RETURN ci.massFrom, s.name, u.mmiCode"
		);

		Record record = result.next();
		assertEquals("12", record.get(0).asString());
		assertEquals("Midazolam", record.get(1).asString());
		assertEquals("MG", record.get(2).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void allIngredientsOfMidazolam() {
		Result result = session.run(
				"MATCH (p:" + PRODUCT_LABEL + " {name: 'Dormicum 15 mg/3 ml'})-->(d:" + DRUG_LABEL + ")" +
						"-->(i:" + MMI_INGREDIENT_LABEL + ")-->(s:" + SUBSTANCE_LABEL + ") " +
						"MATCH (i)-->(u:" + UNIT_LABEL + ") " +
						"RETURN s.name, i.massFrom, u.mmiName, i.isActive"
		);

		for (int i = 0; i < 2; i++) {
			Record record = result.next();
			switch (record.get(0).asString()) {
				case "Midazolamhydrochlorid" -> {
					assertEquals("15", record.get(1).asString());
					assertEquals("mg", record.get(2).asString());
					assertTrue(record.get(3).asBoolean());
				}
				case "Water" -> {
					assertEquals("3", record.get(1).asString());
					assertEquals("ml", record.get(2).asString());
					assertFalse(record.get(3).asBoolean());
				}
				default -> fail("Unexpected ingredient " + record.get(0).asString() + " found!");
			}
		}
		assertFalse(result.hasNext());
	}

	@Test
	public void belocMiteExcludedIngredients() {
		Result result = session.run(
				"MATCH (p:" + PRODUCT_LABEL + " {name: 'Beloc mite'})-->(d:" + DRUG_LABEL + ")" +
						"-->(i:" + MMI_INGREDIENT_LABEL + ")-->(s:" + SUBSTANCE_LABEL + ") " +
						"MATCH (i)-->(u:" + UNIT_LABEL + ") " +
						"RETURN s.name, i.massFrom, u.mmiName, i.isActive"
		);

		assertTrue(result.hasNext());
		Record record = result.next();
		// The other two ingredients carry an unacceptable type code and must thus be excluded. So just 1 match.
		assertFalse(result.hasNext());
		assertEquals("Metoprololtartrat (Ph.Eur.)", record.get(0).asString());
		assertTrue(record.get(3).asBoolean());
	}

	@Test
	public void packageInfo() {
		Result result = session.run(
				"MATCH (p:" + PRODUCT_LABEL + " {mmiId: 0})<-[:" + PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]-" +
						"(pk:" + PACKAGE_LABEL + "{mmiId: 0})--(pz:" + PZN_LABEL + ")" +
						"RETURN pk.onMarketDate, pz.code "
		);

		assertTrue(result.hasNext());
		Record r = result.next();
		assertEquals(LocalDate.of(2022, 1, 12), r.get(0).asLocalDate());
		assertEquals("51465", r.get(1).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void manufacturerAddress() {
		Result result = session.run(
				"MATCH (c:" + COMPANY_LABEL + " {mmiId: 0})-[:" + COMPANY_HAS_ADDRESS_LABEL + "]->(a:" + ADDRESS_LABEL + ") " +
						"RETURN a.street, a.streetNumber, a.postalCode, a.city, a.country, a.countryCode"
		);

		Record record = result.next();
		assertEquals("Arcisstraße", record.get(0).asString());
		assertEquals("21", record.get(1).asString());
		assertEquals("80333", record.get(2).asString());
		assertEquals("München", record.get(3).asString());
		assertEquals("Deutschland", record.get(4).asString());
		assertEquals("DE", record.get(5).asString());
		assertFalse(result.hasNext()); // Only the 'Firmensitz' address (type C) should exist in the db!
	}

	@Test
	public void edqmDoseForms() {
		Result result = session.run(
				"MATCH (p:" + MMI_DOSE_FORM_LABEL + " {mmiName: 'Tbl.'})-[:" + DOSE_FORM_IS_EDQM + "]->" +
						"(e:" + EDQM_LABEL + ":" + EDQM_PDF_LABEL + ")-[:" + EDQM_HAS_CHARACTERISTIC_LABEL + "]->" +
						"(ch:" + EDQM_LABEL + "{type: '" + BASIC_DOSE_FORM.getTypeFullName() + "'})" +
						"RETURN e.code, ch.code "
		);

		Record record = result.next();
		assertEquals(PHARMACEUTICAL_DOSE_FORM.getShorthand() + "-10219000", record.get(0).asString());
		assertEquals(BASIC_DOSE_FORM.getShorthand() + "-0069", record.get(1).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void edqmDoseFormNodesAmount() throws IOException {
		Result result = session.run("MATCH (e:" + EDQM_LABEL + ") RETURN COUNT(e)");

		Record record = result.next();
		assertEquals(getCsvEntries("/edqm_objects.csv"), record.get(0).asInt(),
				"The number of EDQM nodes does not match the number of entries in its CSV source!");
		assertFalse(result.hasNext());
	}

	@Test
	public void edqmDoseFormRelationsAmount() throws IOException {
		Result result = session.run("MATCH (:" + EDQM_LABEL + ")-[r:" + EDQM_HAS_CHARACTERISTIC_LABEL + "]->" +
				"(:" + EDQM_LABEL + ") RETURN COUNT(r)");

		Record record = result.next();
		assertEquals(getCsvEntries("/pdf_relations.csv"), record.get(0).asInt(),
				"The number of EDQM node internal relations does not match the number of entries in its CSV source!");
		assertFalse(result.hasNext());
	}

	@Test
	public void customDoseFormSynonymApplied() {
		Result result = session.run("MATCH (s:" + SYNONYM_LABEL + "{name: 'Filmtbl.'})" +
				"-[r:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(e:" + EDQM_LABEL + "{code: 'PDF-10221000'}) " +
				"RETURN e.name");

		Record record = result.next();
		assertEquals("Film-coated tablet", record.get(0).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void doseFormTranslationApplied() {
		Result result = session.run("MATCH (s:" + SYNONYM_LABEL + "{name: 'Teeaufgusspulver'})" +
				"-[r:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(e:" + EDQM_LABEL + "{code: 'PDF-10202000'}) " +
				"RETURN e.name");

		Record record = result.next();
		assertEquals("Instant herbal tea", record.get(0).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void substanceNameSynonymsExist() {
		Result result = session.run("MATCH (s:" + SYNONYM_LABEL + " {name: 'Midazolamhydrochlorid'})" +
				"-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(t:" + SUBSTANCE_LABEL + "{mmiId: 1}) RETURN t.name");

		Record record = result.next();
		assertEquals("Midazolamhydrochlorid", record.get(0).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void productNameSynonymsExist() {
		Result result = session.run("MATCH (s:" + SYNONYM_LABEL + " {name: 'Dormicum 15 mg/3 ml'})" +
				"-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(t:" + PRODUCT_LABEL + " {mmiId: 0}) RETURN t.name");

		Record record = result.next();
		assertEquals("Dormicum 15 mg/3 ml", record.get(0).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void edqmConceptNameSynonymsExist() {
		Result result = session.run("MATCH (s:" + SYNONYM_LABEL + " {name: 'Oral drops, solution'})" +
				"-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(t:" + EDQM_LABEL + " {code: 'PDF-10101000'}) RETURN t.name");

		Record record = result.next();
		assertEquals("Oral drops, solution", record.get(0).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void emptyAddressWasRemoved() {
		Result result = session.run("MATCH (a:Address {mmiId: 2}) RETURN a");
		assertFalse(result.hasNext());
	}

	@Test
	public void noCodeNodesWithoutCodingSystem() {
		Result result = session.run("MATCH (c:" + CODE_LABEL + ")" +
				"WHERE NOT (c)-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(:" + CODING_SYSTEM_LABEL + ")" +
				" RETURN c");
		assertFalse(result.hasNext());
	}

	@Test
	public void noTemporaryNodes() {
		Result result = session.run("MATCH (a:Temp) RETURN a");
		assertFalse(result.hasNext());
	}

	private void checkProductMatchesIngredient(Record record) {
		String name = record.get(1).asString();
		if (name.equals("Dormicum V 5 mg/5 ml")) {
			assertEquals("5", record.get(2).asString());
		} else {
			assertEquals("15", record.get(2).asString());
		}
	}

	/**
	 * Counts the non-blank lines in the given resource file which do not start with a "#".
	 *
	 * @param resourceName the resource file to read
	 */
	private int getCsvEntries(String resourceName) throws IOException {
		int lines = 0;
		try (InputStream inputStream = GraphDbPopulatorSupport.class.getResourceAsStream(resourceName)) {
			assertNotNull(inputStream);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (!line.startsWith(GraphDbPopulatorSupport.CSV_COMMENT_INDICATOR) && !line.isBlank()) lines++;
				}
			}
		}
		lines--; // Remove header line
		return Math.max(0, lines);
	}

}

//@Disabled("This test wipes the target database. Also it needs to copy files to the Neo4j import directory " +
//		"which is likely different if you have a different OS than mine and also write privileges are required. " +
//		"Sadly, a platform-independent solution is tricky. I have not yet seen a way to inject the test files " +
//		"into the Neo4j harness in a different way.")
class WithoutArchiveIntegrationTest extends IntegrationTest {

	@BeforeAll
	public void integrationTestSetup() throws IOException {
		super.integrationTestSetup(false);
	}

	@Test
	public void noArchivedCompanies() {
		Result result = session.run("MATCH (a:" + COMPANY_LABEL + " {" + ARCHIVED_ATTR + ": true}) RETURN a");
		assertFalse(result.hasNext());
	}

	@Test
	public void noArchivedProducts() {
		Result result = session.run("MATCH (a:" + PRODUCT_LABEL + " {" + ARCHIVED_ATTR + ": true}) RETURN a");
		assertFalse(result.hasNext());
	}

	@Test
	public void noArchivedPackages() {
		Result result = session.run("MATCH (a:" + PACKAGE_LABEL + " {" + ARCHIVED_ATTR + ": true}) RETURN a");
		assertFalse(result.hasNext());
	}

	@Test
	public void noArchivedDrugs() {
		Result result = session.run("MATCH (a:" + DRUG_LABEL + " {" + VIRTUAL_DRUG_ATTR + ": true}) RETURN a");
		assertFalse(result.hasNext());
	}

	@Test
	public void noArchivedIngredients() {
		Result result = session.run("MATCH (a:" + INGREDIENT_LABEL + " {" + ARCHIVED_ATTR + ": true}) RETURN a");
		assertFalse(result.hasNext());
	}

}

@Disabled("This test wipes the target database. Also it needs to copy files to the Neo4j import directory " +
		"which is likely different if you have a different OS than mine and also write privileges are required. " +
		"Sadly, a platform-independent solution is tricky. I have not yet seen a way to inject the test files " +
		"into the Neo4j harness in a different way.")
class WithArchiveIntegrationTest extends IntegrationTest {

	@BeforeAll
	public void integrationTestSetup() throws IOException {
		super.integrationTestSetup(true);
	}

	@Test
	void supercededBelocMiteDoesNotExist() {
		Result result = session.run("MATCH (a:" + PRODUCT_LABEL + " {mmiId: 2}) RETURN a.name");
		assertTrue(result.hasNext());

		// The non-archived version takes priority.
		assertEquals("Beloc mite", result.next().get(0).asString());
		assertFalse(result.hasNext());
	}


	@Test
	void supercededBelocMiteDidNotOverrideCompanyLink() {
		Result result = session.run(
				"MATCH (a:" + PRODUCT_LABEL + " {mmiId: 2})<-[:" + MANUFACTURES_LABEL + "]-(c:" + COMPANY_LABEL + ") RETURN c.mmiId");
		assertTrue(result.hasNext());

		// The non-archived version takes priority.
		assertEquals(0, result.next().get(0).asInt());
		assertFalse(result.hasNext());
	}

	@Test
	void plainName() {
		Result result = session.run("MATCH (a:" + PRODUCT_LABEL + " {mmiId: 3002}) RETURN a.name, a.archived");
		assertTrue(result.hasNext());

		Record record = result.next();
		assertEquals("H2O but very expensive", record.get(0).asString());
		assertTrue(record.get(1).asBoolean());
		assertFalse(result.hasNext());
	}

	@Test
	void pharmaceuticalFlagChecked() {
		// This product has a PHARMACEUTICAL_FLAG=0 and thus should not be loaded.
		Result result = session.run("MATCH (p:" + PRODUCT_LABEL + " {mmiId: 3003}) RETURN p");
		assertFalse(result.hasNext());
	}

	@Test
	void companyLinkToExistingCompany() {
		Result result = session.run(
				"MATCH (p:" + PRODUCT_LABEL + " {mmiId: 3001})<-[:" + MANUFACTURES_LABEL + "]-(c:" + COMPANY_LABEL + ") RETURN c.mmiId");
		assertTrue(result.hasNext());
		assertEquals(1, result.next().get(0).asInt());
		assertFalse(result.hasNext());
	}

	@Test
	void companyLinkToArchivedCompany() {
		Result result = session.run(
				"MATCH (p:" + PRODUCT_LABEL + " {mmiId: 3002})<-[:" + MANUFACTURES_LABEL + "]-(c:" + COMPANY_LABEL + " {" + ARCHIVED_ATTR + ": true}) RETURN c.mmiId");
		assertTrue(result.hasNext());
		assertEquals(3, result.next().get(0).asInt());
		assertFalse(result.hasNext());
	}

	@Test
	void archivedDrugsAndIngredients() {
		Result result = session.run("MATCH (p:" + PRODUCT_LABEL + " {mmiId: 3001})" +
				"-[:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d:" + DRUG_LABEL + ")" +
				"-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i:" + INGREDIENT_LABEL + ")" +
				"-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(s:" + SUBSTANCE_LABEL + ") " +
				"MATCH (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(u:" + UNIT_LABEL + ") " +
				"MATCH (d)-[:" + DRUG_HAS_UNIT_LABEL + "]->(du:" + UNIT_LABEL + ") " +
				"RETURN id(d) as drugId, d.amount as drugAmount, d." + VIRTUAL_DRUG_ATTR + " as virtual, " +
				"i.massFrom as massFrom, i.massTo as massTo, i.isActive as active," +
				"s.mmiId as substanceId, u.ucumCs as unit, du.ucumCs as drugUnit");

		List<Record> records = List.of(result.next(), result.next(), result.next());
		// If below fails, it's likely the ingredient marked as "equivalence" has not been excluded.
		assertFalse(result.hasNext());

		Record d1 = records.stream().filter(r -> r.get("substanceId").asInt() == 3).findFirst().orElseThrow();
		Record d2 = records.stream().filter(r -> r.get("substanceId").asInt() == 1).findFirst().orElseThrow();
		Record d3 = records.stream().filter(r -> r.get("substanceId").asInt() == 4).findFirst().orElseThrow();

		records.forEach(d -> {
			assertTrue(d.get("virtual").asBoolean());
		});

		assertTrue(d1.get("active").asBoolean());
		assertTrue(d2.get("active").asBoolean());
		assertFalse(d3.get("active").asBoolean());

		// Ingredient 1 and 2 report the same drug amounts (5 ml), so they must be the same drug node
		assertEquals(d1.get("drugId"), d2.get("drugId"));
		// Ingredient 3 has a different drug amount (10 ml), so it must have a different drug node
		assertNotEquals(d1.get("drugId"), d3.get("drugId"));

		assertEquals("5", d1.get("drugAmount").asString());
		assertEquals("ml", d1.get("drugUnit").asString());
		assertEquals("10", d3.get("drugAmount").asString());
		assertEquals("ml", d1.get("drugUnit").asString());

		assertEquals("50", d1.get("massFrom").asString());
		assertEquals("60", d1.get("massTo").asString());
		assertEquals("mg", d1.get("unit").asString());
		assertEquals("5", d2.get("massFrom").asString());
		assertTrue(d2.get("massTo").isNull());
		assertEquals("mg", d2.get("unit").asString());
		assertEquals("10", d3.get("massFrom").asString());
		assertTrue(d3.get("massTo").isNull());
		assertEquals("ml", d3.get("unit").asString());
	}

	@Test
	void archivedIngredientInfoNotApplied() {
		Result result = session.run("MATCH (p:" + PRODUCT_LABEL + " {mmiId: 2})" +
				"-[:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d:" + DRUG_LABEL + ")" +
				"-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i:" + INGREDIENT_LABEL + ")" +
				"-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(s:" + SUBSTANCE_LABEL + " {mmiId: 1}) " +
				"RETURN p");
		assertFalse(result.hasNext());
	}

	@Test
	void archivedPackages() {
		Result result = session.run("MATCH (p:" + PRODUCT_LABEL + ")-[:" + PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]-" +
				"(pk:" + PACKAGE_LABEL + " {" + ARCHIVED_ATTR + ": true})" +
				"<-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]-(c:" + PZN_LABEL + ") " +
				"RETURN p.mmiId, pk.mmiId, pk.name, pk.onMarketDate, pk.offMarketDate, c.code");

		List<Record> results = List.of(
				result.next(),
				result.next(),
				result.next());
		assertFalse(result.hasNext());

		Record p1 = results.stream().filter(r -> r.get(1).asInt() == 3001).findAny().orElseThrow();
		Record p2 = results.stream().filter(r -> r.get(1).asInt() == 3002).findAny().orElseThrow();
		Record p3 = results.stream().filter(r -> r.get(1).asInt() == 3003).findAny().orElseThrow();

		assertEquals(0, p1.get(0).asInt());
		assertEquals("Old Dormicum 15 mg/3 ml", p1.get(2).asString());
		assertEquals(LocalDate.of(2020, 3, 18), p1.get(3).asLocalDate());
		assertEquals(LocalDate.of(2022, 1, 12), p1.get(4).asLocalDate());
		assertEquals("51464", p1.get(5).asString());

		assertEquals(3002, p2.get(0).asInt());
		assertEquals("Very expensive water", p2.get(2).asString());
		assertEquals(LocalDate.of(2000, 1, 1), p2.get(3).asLocalDate());
		assertEquals(LocalDate.of(2010, 1, 1), p2.get(4).asLocalDate());
		assertEquals("31843", p2.get(5).asString());

		assertEquals(3002, p3.get(0).asInt());
		assertEquals("Very expensive water", p3.get(2).asString());
		assertEquals(LocalDate.of(2010, 1, 1), p3.get(3).asLocalDate());
		assertEquals(LocalDate.of(2022, 8, 17), p3.get(4).asLocalDate());
		assertEquals("31844", p3.get(5).asString());
	}

	@Test
	void packageInterconnection1() {
		Result result = session.run("MATCH (p:" + PACKAGE_LABEL + " {mmiId: 3001})" +
				"-[:" + PACKAGE_HAS_SUCCESSOR_LABEL + "]->(p2:" + PACKAGE_LABEL + ") " +
				"RETURN p2.mmiId");
		assertTrue(result.hasNext());
		assertEquals(0, result.next().get(0).asInt());
		assertFalse(result.hasNext());
	}

	@Test
	void packageInterconnection2() {
		Result result = session.run("MATCH (p:" + PACKAGE_LABEL + " {mmiId: 3002})" +
				"-[:" + PACKAGE_HAS_SUCCESSOR_LABEL + "]->(p2:" + PACKAGE_LABEL + ") " +
				"RETURN p2.mmiId");
		assertTrue(result.hasNext());
		assertEquals(3003, result.next().get(0).asInt());
		assertFalse(result.hasNext());
	}

	@Test
	void packageInterconnection3() {
		Result result = session.run("MATCH (p:" + PACKAGE_LABEL + " {mmiId: 3003})" +
				"-[:" + PACKAGE_HAS_SUCCESSOR_LABEL + "]->(p2:" + PACKAGE_LABEL + ") " +
				"RETURN p2.mmiId");
		assertFalse(result.hasNext());
	}

	@Test
	void supercededCompany() {
		Result result = session.run("MATCH (c:" + COMPANY_LABEL + " {mmiId: 0}) RETURN c.name, c." + ARCHIVED_ATTR);
		assertTrue(result.hasNext());
		Record r = result.next();
		assertEquals("Sample Company Inc.", r.get(0).asString());
		assertTrue(r.get(1).isNull());
		assertFalse(result.hasNext());
	}

	@Test
	void archivedCompany() {
		Result result = session.run(
				"MATCH (c:" + COMPANY_LABEL + " {mmiId: 3}) RETURN c.name, c.shortName, c." + ARCHIVED_ATTR);
		assertTrue(result.hasNext());
		Record r = result.next();
		assertEquals("Old Rusty Parts Inc.", r.get(0).asString());
		assertTrue(r.get(2).asBoolean());
		assertFalse(result.hasNext());
	}

}