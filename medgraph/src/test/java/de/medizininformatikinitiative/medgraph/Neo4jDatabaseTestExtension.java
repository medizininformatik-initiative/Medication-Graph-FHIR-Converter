package de.medizininformatikinitiative.medgraph;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

/**
 * Test extension which provides a local JVM Neo4j database instance for testing. Test classes can use
 * {@link ExtendWith} to include this extension, allowing them to access the Neo4j instance using
 * {@link #getDatabase()}. An easier way is to extend {@link Neo4jTest}, which is already correctly configured.
 * <p>
 * The database is shared among all test classes using it and not reset between tests or test classes in any way. Write
 * access to the database is therefore highly discouraged.
 *
 * @author Markus Budeus
 * @see Neo4jTest
 */
public class Neo4jDatabaseTestExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

	private static volatile Neo4j neo4j;
	private static volatile DatabaseConnection connection;

	/**
	 * Returns the database instance.
	 *
	 * @throws IllegalStateException if the database instance has not been started
	 */
	public static Neo4j getDatabase() {
		Neo4j db = Neo4jDatabaseTestExtension.neo4j;
		if (db == null) throw new IllegalStateException("The Neo4j database instance has not yet been started! " +
				"Maybe your test case is missing the" +
				" @ExtendWith(" + Neo4jDatabaseTestExtension.class.getSimpleName() + ".class) annotation");
		return db;
	}

	public static DatabaseConnection getConnection() {
		DatabaseConnection con = Neo4jDatabaseTestExtension.connection;
		if (con == null) throw new IllegalStateException("The Neo4j database instance has not yet been started! " +
				"Maybe your test case is missing the" +
				" @ExtendWith(" + Neo4jDatabaseTestExtension.class.getSimpleName() + ".class) annotation");
		return con;
	}

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		if (Neo4jDatabaseTestExtension.neo4j == null) {
			synchronized (Neo4jDatabaseTestExtension.class) {
				if (Neo4jDatabaseTestExtension.neo4j == null) {
					initializeDatabase();
					registerShutdownHook(extensionContext);
				}
			}
		}
	}

	private static void initializeDatabase() {
		Neo4jDatabaseTestExtension.neo4j = Neo4jBuilders.newInProcessBuilder()
		                                                .withFixture(getDatabaseFixture())
		                                                .build();

		DatabaseConnection.setConnection(
				neo4j.boltURI().toString(),
				"neo4j",
				"neo4j".toCharArray(),
				DatabaseConnection.SaveOption.DONT_SAVE
		);

		Neo4jDatabaseTestExtension.connection = new DatabaseConnection();
	}

	private void registerShutdownHook(ExtensionContext extensionContext) {
		extensionContext.getRoot().getStore(ExtensionContext.Namespace.GLOBAL)
		                .put("Neo4j Test Database Manager", this);
	}

	private static String getDatabaseFixture() {
		// Database definition created in arrows.app and exported as Cypher.
		return """
				CREATE (:EDQM:Code {code: "PDF-10204000", name: "Granules", status: "Current", intendedSite: "Oral"})<-[:CORRESPONDS_TO]-(:DoseForm {mmiCode: 20, mmiName: "Gran. zum Einnehmen", mmiDesc: "Zum Einnehmen"})<-[:HAS_DOSE_FORM]-(n15:Drug {amount: 1, mmiId: 1})-[:CONTAINS]->(n1:MmiIngredient:Ingredient {isActive: "true", massFrom: 500, mmiId: 1})-[:IS_SUBSTANCE]->(:Substance {name: "Acetylsalicylsäure", mmiId: 1})<-[:REFERENCES]-(:Name {name: "Aspirin"}),
				(:Address {street: "Kaiser-Wilhelm-Allee", streetNumber: 56, postalCode: 51368, city: "Leverkusen", country: "Deutschland", countryCode: "DE", mmiId: 1})<-[:HAS_ADDRESS]-(:Company {name: "Bayer Vital GmbH", shortName: "Bayer Vital", mmiId: 1})-[:MANUFACTURES]->(n0:Product {name: "Aspirin Complex Granulat-Sticks 500 mg/30 mg Granulat", mmiId: 1})-[:CONTAINS]->(n15),
				(n1)-[:HAS_UNIT]->(n6:Unit:UCUM {print: "mg", ucumCs: "mg", ucumCi: "MG", mmiCode: "MG", mmiName: "mg"})<-[:HAS_UNIT]-(n23:MmiIngredient:Ingredient {isActive: "true", massFrom: "16,68", mmiId: 2})-[:CORRESPONDS_TO]->(n37:Ingredient {massFrom: 15})-[:IS_SUBSTANCE]->(n38:Substance {name: "Midazolam", mmiId: 2})<-[:REFERENCES]-(:Name {name: "Midazolam"}),
				(:Name {name: "Aspirin Complex Granulat-Sticks"})-[:REFERENCES]->(n0),
				(:EDQM:Code {code: "PDF-50060000", name: "	Solution for injection/infusion", status: "Current", intendedSite: "Parenteral"})<-[:CORRESPONDS_TO]-(n30:DoseForm {mmiCode: 518, mmiName: "Injektions-/Infusionslsg.", mmiDesc: "parenteral"})<-[:HAS_DOSE_FORM]-(n29:Drug {amount: 3, mmiId: 2})-[:CONTAINS]->(n23)-[:IS_SUBSTANCE]->(:Substance {name: "Midazolam hydrochlorid", mmiId: 3})<-[:IS_SUBSTANCE]-(n34:MmiIngredient:Ingredient {isActive: "true", massFrom: "5,5", massTo: "5,7", mmiId: 3})-[:HAS_UNIT]->(n6)<-[:HAS_UNIT]-(n37),
				(n30)<-[:HAS_DOSE_FORM]-(n36:Drug {amount: 3, mmiId: 3})<-[:CONTAINS]-(:Product {name: "Dormicum® V 5 mg/5 ml Injektionslösung", mmiId: 3})<-[:REFERENCES]-(:Name {name: "Dormicum"})-[:REFERENCES]->(:Product {name: "Dormicum® 15 mg/3 ml Injektionslösung", mmiId: 2})-[:CONTAINS]->(n29)-[:HAS_UNIT]->(n32:Unit:UCUM {print: "ml", ucumCs: "ml", ucumCi: "ML", mmiCode: "ML", mmiName: "ml"})<-[:HAS_UNIT]-(n36)-[:CONTAINS]->(n50:MmiIngredient:Ingredient {isActive: "false", massFrom: 5, mmiId: 6})-[:HAS_UNIT]->(n32),
				(n36)-[:CONTAINS]->(n34)-[:CORRESPONDS_TO]->(n39:Ingredient {massFrom: 5})-[:HAS_UNIT]->(n6),
				(n39)-[:IS_SUBSTANCE]->(n38),
				(:Name {name: "Adrenalin"})-[:REFERENCES]->(:Substance {name: "Epinephrin", mmiId: 4})<-[:IS_SUBSTANCE]-(n43:MmiIngredient:Ingredient {isActive: "true", massFrom: 300, mmiId: 4})<-[:CONTAINS]-(n44:Drug {amount: "0,3", mmiId: 4})<-[:CONTAINS]-(:Product {name: "Anapen 300 µg kohlpharma Injektionslösung", mmiId: 4}),
				(n43)-[:HAS_UNIT]->(:Unit:UCUM {print: "μg", ucumCs: "ug", ucumCi: "UG", mmiCode: "MCG", mmiName: "μg"}),
				(n50)-[:IS_SUBSTANCE]->(n48:Substance {name: "Wasser für Injektionszwecke", mmiId: 5})<-[:IS_SUBSTANCE]-(n51:MmiIngredient:Ingredient {isActive: "false", massFrom: 3, mmiId: 5})-[:HAS_UNIT]->(n32)<-[:HAS_UNIT]-(n44)-[:HAS_DOSE_FORM]->(:DoseForm {mmiCode: 220, mmiName: "Injektionslsg.", mmiDesc: "parenteral"})-[:CORRESPONDS_TO]->(:EDQM:Code {code: "PDF-11201000", name: "Solution for injection", status: "Current", intendedSite: "Parenteral"}),
				(:Name {name: "Wasser"})-[:REFERENCES]->(n48),
				(n29)-[:CONTAINS]->(n51)
				"""
				.stripIndent()
				.replace("\"true\"", "true") // Arrows.app does not detect booleans...
				.replace("\"false\"", "false")
				.replaceAll("mass(From|To): ([\\d]+)", "mass$1: \"$2\"") // massFrom and massTo shall always be strings
				.replaceAll("amount: ([\\d]+)", "amount: \"$1\"") // Same goes for amount
				;
	}

	@Override
	public void close() {
		getDatabase().close();
		Neo4jDatabaseTestExtension.neo4j = null;
	}
}
