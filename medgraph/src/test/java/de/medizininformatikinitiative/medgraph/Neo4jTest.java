package de.medizininformatikinitiative.medgraph;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Session;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;


/**
 * Test class for tests using a local Neo4j Instance.
 *
 * @author Markus Budeus
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Neo4jTest extends UnitTest {

	protected Neo4j neo4j;

	protected DatabaseConnection connection;
	protected Session session;

	@BeforeAll
	void initializeNeo4j() {
		this.neo4j = Neo4jBuilders.newInProcessBuilder()
		                          .withFixture(getDatabaseFixture())
		                          .build();

		DatabaseConnection.setConnection(
				neo4j.boltURI().toString(),
				"neo4j",
				"neo4j".toCharArray(),
				DatabaseConnection.SaveOption.DONT_SAVE
		);

		connection = new DatabaseConnection();
		session = connection.createSession();
	}

	private String getDatabaseFixture() {
		// Database definition created in arrows.app and exported as Cypher.
		return """
				CREATE (:EDQM:Code {code: "PDF-10204000", name: "Granules", status: "Current", intendedSite: "Oral"})<-[:CORRESPONDS_TO]-(:DoseForm {mmiCode: 20, mmiName: "Gran. zum Einnehmen", mmiDesc: "Zum Einnehmen"})<-[:HAS_DOSE_FORM]-(n15:Drug {amount: 1, mmiId: 1})-[:CONTAINS]->(n1:MmiIngredient:Ingredient {isActive: "true", massFrom: 500, mmiId: 1})-[:IS_SUBSTANCE]->(:Substance {name: "Acetylsalicylsäure", mmiId: 1})<-[:REFERENCES]-(:Name {name: "Aspirin"}),
                (:Address {street: "Kaiser-Wilhelm-Allee", streetNumber: 56, postalCode: 51368, city: "Leverkusen", country: "Deutschland", countryCode: "DE", mmiId: 1})<-[:HAS_ADDRESS]-(:Company {name: "Bayer Vital GmbH", shortName: "Bayer Vital", mmiId: 1})-[:MANUFACTURES]->(n0:Product {name: "Aspirin Complex Granulat-Sticks 500 mg/30 mg Granulat", mmiId: 1})-[:CONTAINS]->(n15)-[:HAS_UNIT]->(n32:Unit:UCUM {print: "ml", ucumCs: "ml", ucumCi: "ML", mmiCode: "ML", mmiName: "ml"})<-[:HAS_UNIT]-(n36:Drug {amount: 3, mmiId: 3}),
                (n1)-[:HAS_UNIT]->(n6:Unit:UCUM {print: "mg", ucumCs: "mg", ucumCi: "MG", mmiCode: "MG", mmiName: "mg"})<-[:HAS_UNIT]-(n23:MmiIngredient:Ingredient {isActive: "true", massFrom: "16,68", mmiId: 2})-[:CORRESPONDS_TO]->(n37:Ingredient {massFrom: 15})-[:IS_SUBSTANCE]->(n38:Substance {name: "Midazolam", mmiId: 2})<-[:REFERENCES]-(:Name {name: "Midazolam"}),
                (:Name {name: "Aspirin Complex Granulat-Sticks"})-[:REFERENCES]->(n0),
                (:EDQM:Code {code: "PDF-50060000", name: "	Solution for injection/infusion", status: "Current", intendedSite: "Parenteral"})<-[:CORRESPONDS_TO]-(n30:DoseForm {mmiCode: 518, mmiName: "Injektions-/Infusionslsg.", mmiDesc: "parenteral"})<-[:HAS_DOSE_FORM]-(n29:Drug {amount: 3, mmiId: 2})-[:CONTAINS]->(n23)-[:IS_SUBSTANCE]->(:Substance {name: "Midazolam hydrochlorid", mmiId: 3})<-[:IS_SUBSTANCE]-(n34:MmiIngredient:Ingredient {isActive: "true", massFrom: "5,56", mmiId: 3})-[:HAS_UNIT]->(n6)<-[:HAS_UNIT]-(n37),
                (n30)<-[:HAS_DOSE_FORM]-(n36)<-[:CONTAINS]-(:Product {name: "Dormicum® V 5 mg/5 ml Injektionslösung", mmiId: 3})<-[:REFERENCES]-(:Name {name: "Dormicum"})-[:REFERENCES]->(:Product {name: "Dormicum® 15 mg/3 ml Injektionslösung", mmiId: 2})-[:CONTAINS]->(n29)-[:HAS_UNIT]->(n32),
                (n36)-[:CONTAINS]->(n34)-[:CORRESPONDS_TO]->(n39:Ingredient {massFrom: 5})-[:HAS_UNIT]->(n6),
                (n39)-[:IS_SUBSTANCE]->(n38)
				"""
				.replace("\"true\"", "true") // Arrows.app does not detect booleans...
				.replace("\"false\"", "false");
	}

	@AfterAll
	void closeNeo4j() {
		session.close();
		connection.close();
		neo4j.close();
	}


}
