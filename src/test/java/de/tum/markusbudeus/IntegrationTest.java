package de.tum.markusbudeus;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static de.tum.markusbudeus.DatabaseDefinitions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs the whole migration on a set of sample files.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTest {

	// WARNING:
	// This test will completely overwrite the target database

	private DatabaseConnection connection;
	private Session session;

	@BeforeAll
	@SuppressWarnings("ConstantConditions")
	public void integrationTestSetup() throws URISyntaxException {
		Path sampleFilesPath = Path.of(IntegrationTest.class.getClassLoader().getResource("sample").toURI());
		connection = new DatabaseConnection();
		session = connection.createSession();
		session.run(new Query("MATCH (n) DETACH DELETE n")).consume(); // Delete everything
		try {
			Main.runMigrators(sampleFilesPath, false);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Integration test failed!", e);
		}
	}

	@Test
	public void midazolamAskCode() {
		Result result = session.run("MATCH (a:"+ASK_LABEL+")-[:"+CODE_REFERENCE_RELATIONSHIP_NAME+"]->(s:"+SUBSTANCE_LABEL+" {name: 'Midazolam'}) RETURN a.code");
		assertEquals("22661", result.next().get(0).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void midazolamCasCode() {
		Result result = session.run("MATCH (a:"+CAS_LABEL+")-[:"+CODE_REFERENCE_RELATIONSHIP_NAME+"]->(s:"+SUBSTANCE_LABEL+" {name: 'Midazolam'}) RETURN a.code");
		assertEquals("59467-70-8", result.next().get(0).asString());
		assertFalse(result.hasNext());
	}

	@Test
	public void productsContainingMidazolamhydrochlorid() {
		Result result = session.run(
				"MATCH (p:"+PRODUCT_LABEL+")-[c1:"+PRODUCT_CONTAINS_DRUG_LABEL+"]->" +
						"(d:"+DRUG_LABEL+")-[c2:"+DRUG_CONTAINS_INGREDIENT_LABEL+"]->" +
						"(i:"+INGREDIENT_LABEL+")-[c3:"+INGREDIENT_IS_SUBSTANCE_LABEL+"]->" +
						"(s:"+SUBSTANCE_LABEL+" {name: 'Midazolamhydrochlorid'}) " +
						"RETURN s,p.name,i.mass_to"
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
				"MATCH (m:"+COMPANY_LABEL+")-[r:"+MANUFACTURES_LABEL+"]-(p:"+PRODUCT_LABEL+") RETURN p.mmi_id"
		);

		boolean[] mmiIdIncluded = new boolean[] { false, false, false };

		while (result.hasNext()) {
			Record record = result.next();
			int mmiId = record.get(0).asInt();
			if (mmiIdIncluded[mmiId]) {
				fail("The product with mmi_id "+mmiId+" was included twice in the result!");
			}
			mmiIdIncluded[mmiId] = true;
		}

		assertTrue(mmiIdIncluded[0]);
		assertTrue(mmiIdIncluded[1]);
		assertTrue(mmiIdIncluded[2]);
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
