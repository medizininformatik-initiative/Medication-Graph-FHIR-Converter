package de.medizininformatikinitiative.medgraph.graphdbpopulator;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders.EdqmStandardTermsLoader;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders.Loader;
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
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs the whole migration on a set of sample files.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("This test wipes the target database. Also it needs to copy files to the Neo4j import directory " +
		"which is likely different if you have a different OS than mine and also write privileges are required. " +
		"Sadly, a platform-independent solution is tricky. I have not yet seen a way to inject the test files " +
		"into the Neo4j harness in a different way.")
public class IntegrationTest {

	private DatabaseConnection connection;
	private Session session;

	@BeforeAll
	public void integrationTestSetup() throws IOException {
		connection = DatabaseConnection.createDefault();
		session = connection.createSession();
		copyTestFilesToNeo4jImportDir();

		GraphDbPopulator graphDbPopulator = new GraphDbPopulator();
		graphDbPopulator.clearDatabase(session); // Delete everything
		graphDbPopulator.prepareLoaders(session).forEach(Loader::execute);
	}

	@AfterAll
	public void cleanup() throws IOException {
		session.close();
		connection.close();
		deleteTestFilesFromNeo4jImportDir();
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
				"MATCH (m:" + COMPANY_LABEL + ")-[r:" + MANUFACTURES_LABEL + "]-(p:" + PRODUCT_LABEL + ") RETURN p.mmiId"
		);

		boolean[] mmiIdIncluded = new boolean[]{false, false, false};

		while (result.hasNext()) {
			Record record = result.next();
			int mmiId = record.get(0).asInt();
			if (mmiIdIncluded[mmiId]) {
				fail("The product with mmi_id " + mmiId + " was included twice in the result!");
			}
			mmiIdIncluded[mmiId] = true;
		}

		assertTrue(mmiIdIncluded[0]);
		assertTrue(mmiIdIncluded[1]);
		assertTrue(mmiIdIncluded[2]);
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
	public void packageInfo() {
		Result result = session.run(
				"MATCH (p:" + PRODUCT_LABEL + " {mmiId: 0})<-[:" + PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]-(pk:" + PACKAGE_LABEL + ")--(pz:" + PZN_LABEL + ")" +
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
						"(ch:" + EDQM_LABEL + "{type: '" + EdqmStandardTermsLoader.EDQM_BDF_TYPE + "'})" +
						"RETURN e.code, ch.code "
		);

		Record record = result.next();
		assertEquals(EdqmStandardTermsLoader.EDQM_PDF_CLASS + "-10219000", record.get(0).asString());
		assertEquals(EdqmStandardTermsLoader.EDQM_BDF_CLASS + "-0069", record.get(1).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void edqmDoseFormNodesAmount() throws IOException {
		Result result = session.run("MATCH (e:" + EDQM_LABEL + ") RETURN COUNT(e)");

		Record record = result.next();
		assertEquals(getCsvEntries("/edqm_objects.csv", true), record.get(0).asInt(),
				"The number of EDQM nodes does not match the number of entries in its CSV source!");
		assertFalse(result.hasNext());
	}

	@Test
	public void edqmDoseFormRelationsAmount() throws IOException {
		Result result = session.run("MATCH (:" + EDQM_LABEL + ")-[r:" + EDQM_HAS_CHARACTERISTIC_LABEL + "]->" +
				"(:" + EDQM_LABEL + ") RETURN COUNT(r)");

		Record record = result.next();
		assertEquals(getCsvEntries("/pdf_relations.csv", true), record.get(0).asInt(),
				"The number of EDQM node internal relations does not match the number of entries in its CSV source!");
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
	 * @param hasHeader    if true, additionally deducts 1 from the result to account for a header line
	 */
	private int getCsvEntries(String resourceName, boolean hasHeader) throws IOException {
		int lines = 0;
		try (InputStream inputStream = GraphDbPopulator.class.getResourceAsStream(resourceName)) {
			assertNotNull(inputStream);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (!line.startsWith(GraphDbPopulator.CSV_COMMENT_INDICATOR) && !line.isBlank()) lines++;
				}
			}
		}
		if (hasHeader) lines--;
		return Math.max(0, lines);
	}

	private void copyTestFilesToNeo4jImportDir() throws IOException {
		new GraphDbPopulator().copyKnowledgeGraphSourceDataToNeo4jImportDirectory(
				Path.of("src", "test", "resources", "sample"),
				Path.of("/var", "lib", "neo4j", "import")
		);
	}

	private void deleteTestFilesFromNeo4jImportDir() throws IOException {
		new GraphDbPopulator().removeFilesFromNeo4jImportDir(
				Path.of("/var", "lib", "neo4j", "import")
		);
	}

}
