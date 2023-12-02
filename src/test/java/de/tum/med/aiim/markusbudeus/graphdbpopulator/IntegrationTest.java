package de.tum.med.aiim.markusbudeus.graphdbpopulator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs the whole migration on a set of sample files.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTest {

	// WARNING:
	// This test will completely overwrite the target database

	// Command to load test data:
	// sudo mv /var/lib/neo4j/import/mmi_pharmindex /var/lib/neo4j/import/mmi_pharmindex-2; sudo mkdir /var/lib/neo4j/import/mmi_pharmindex; sudo cp src/test/resources/sample/*.CSV /var/lib/neo4j/import/mmi_pharmindex
	// Command to unload test data:
	// sudo rm -rf /var/lib/neo4j/import/mmi_pharmindex; sudo mv /var/lib/neo4j/import/mmi_pharmindex-2 /var/lib/neo4j/import/mmi_pharmindex

	private DatabaseConnection connection;
	private Session session;

	@BeforeAll
	public void integrationTestSetup() {
		connection = new DatabaseConnection();
		session = connection.createSession();
		session.run(new Query("MATCH (n) DETACH DELETE n")).consume(); // Delete everything
		try {
			Main.runMigrators();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Integration test failed!", e);
		}
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
						"-[df:" + DRUG_HAS_DOSE_FORM_LABEL + "]->(f:" + DOSE_FORM_LABEL + ")-[:" + DOSE_FORM_IS_EDQM + "]->(e:" + EDQM_LABEL + ") " +
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
						"-[df:" + DRUG_HAS_DOSE_FORM_LABEL + "]->(f:" + DOSE_FORM_LABEL + ")-[:" + DOSE_FORM_IS_EDQM + "]->(e:" + EDQM_LABEL + ") " +
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

	private void checkProductMatchesIngredient(Record record) {
		String name = record.get(1).asString();
		if (name.equals("Dormicum V 5 mg/5 ml")) {
			assertEquals("5", record.get(2).asString());
		} else {
			assertEquals("15", record.get(2).asString());
		}
	}

	public void tearDown() {
		session.close();
		connection.close();
	}

}
