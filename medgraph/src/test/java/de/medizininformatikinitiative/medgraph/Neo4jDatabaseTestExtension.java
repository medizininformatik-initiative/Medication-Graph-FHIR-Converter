package de.medizininformatikinitiative.medgraph;

import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.GraphDbPopulator;
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

	private static volatile ConnectionConfiguration originalDefaultConfig;

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
	public void beforeAll(ExtensionContext extensionContext) {
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

		ConnectionConfiguration config = new ConnectionConfiguration(
				neo4j.boltURI().toString(),
				"neo4j",
				"neo4j".toCharArray()
		);

		originalDefaultConfig = ConnectionConfiguration.getDefault();
		ConnectionConfiguration.setDefault(config);

		Neo4jDatabaseTestExtension.connection = config.createConnection();
	}

	private void registerShutdownHook(ExtensionContext extensionContext) {
		extensionContext.getRoot().getStore(ExtensionContext.Namespace.GLOBAL)
		                .put("Neo4j Test Database Manager", this);
	}

	/**
	 * Returns the Cypher statement which initializes the test database.
	 */
	public static String getDatabaseFixture() {
		// Database definition created in arrows.app and exported as Cypher.
		return """
				CREATE (:EDQM:Code:DoseForm {code: "PDF-10204000", name: "Granules", status: "Current", intendedSite: "Oral"})<-[:CORRESPONDS_TO]-(:MmiDoseForm {mmiCode: 20, mmiName: "Gran. zum Einnehmen", mmiDesc: "Zum Einnehmen"})<-[:HAS_DOSE_FORM]-(n15:Drug {amount: 1, mmiId: 1})-[:CONTAINS]->(n1:MmiIngredient:Ingredient {isActive: "true", massFrom: 500, mmiId: 1})-[:IS_SUBSTANCE]->(:Substance {name: "Acetylsalicylsäure", mmiId: 1})<-[:REFERENCES]-(:Name {name: "Aspirin"}),
				(:Address {street: "Kaiser-Wilhelm-Allee", streetNumber: 56, postalCode: 51368, city: "Leverkusen", country: "Deutschland", countryCode: "DE", mmiId: 1})<-[:HAS_ADDRESS]-(:Company {name: "Bayer Vital GmbH", shortName: "Bayer Vital", mmiId: 1})-[:MANUFACTURES]->(n0:Product {name: "Aspirin Complex Granulat-Sticks 500 mg/30 mg Granulat", mmiId: 1})-[:CONTAINS]->(n15),
				(n1)-[:HAS_UNIT]->(n6:Unit:UCUM {print: "mg", ucumCs: "mg", ucumCi: "MG", mmiCode: "MG", mmiName: "mg"})<-[:HAS_UNIT]-(n23:MmiIngredient:Ingredient {isActive: "true", massFrom: "16,68", mmiId: 2})-[:CORRESPONDS_TO]->(n37:Ingredient {massFrom: 15})-[:IS_SUBSTANCE]->(n38:Substance {name: "Midazolam", mmiId: 2})<-[:REFERENCES]-(:Name {name: "Midazolam"}),
				(:Name {name: "Aspirin Complex Granulat-Sticks"})-[:REFERENCES]->(n0)<-[:BELONGS_TO]-(:Package {amount: 5, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "5 st", mmiId: 1, name: "Aspirin Complex 5 Granulat-Sticks", onMarketDate: "2014-07-15"})<-[:REFERENCES]-(:PZN:Code {code: 10000001}),
				(:EDQM:Code:DoseForm {code: "PDF-50060000", name: "Solution for injection/infusion", status: "Current", intendedSite: "Parenteral"})<-[:CORRESPONDS_TO]-(n30:MmiDoseForm {mmiCode: 518, mmiName: "Injektions-/Infusionslsg.", mmiDesc: "parenteral"})<-[:HAS_DOSE_FORM]-(n29:Drug {amount: 3, mmiId: 2})-[:CONTAINS]->(n23)-[:IS_SUBSTANCE]->(:Substance {name: "Midazolam hydrochlorid", mmiId: 3})<-[:IS_SUBSTANCE]-(n34:MmiIngredient:Ingredient {isActive: "true", massFrom: "5,5", massTo: "5,7", mmiId: 3})-[:HAS_UNIT]->(n6)<-[:HAS_UNIT]-(n37),
				(n30)<-[:HAS_DOSE_FORM]-(n36:Drug {amount: 5, mmiId: 3})<-[:CONTAINS]-(n33:Product {name: "Dormicum® V 5 mg/5 ml Injektionslösung", mmiId: 3})<-[:REFERENCES]-(:Name {name: "Dormicum"})-[:REFERENCES]->(n22:Product {name: "Dormicum® 15 mg/3 ml Injektionslösung", mmiId: 2})-[:CONTAINS]->(n29)-[:HAS_UNIT]->(n32:Unit:UCUM {print: "ml", ucumCs: "ml", ucumCi: "ML", mmiCode: "ML", mmiName: "ml"})<-[:HAS_UNIT]-(n36)-[:CONTAINS]->(n50:MmiIngredient:Ingredient {isActive: "false", massFrom: 5, mmiId: 6})-[:IS_SUBSTANCE]->(n48:Substance {name: "Wasser für Injektionszwecke", mmiId: 5})<-[:IS_SUBSTANCE]-(n72:MmiIngredient:Ingredient {isActive: "false", mmiId: 7}),
				(n36)-[:CONTAINS]->(n34)-[:CORRESPONDS_TO]->(n39:Ingredient {massFrom: 5})-[:HAS_UNIT]->(n6)<-[:HAS_UNIT]-(n76:MmiIngredient:Ingredient {isActive: "true", mmiId: 8, massFrom: "10,48"})-[:CORRESPONDS_TO]->(n77:Ingredient {massFrom: "7,83"})-[:IS_SUBSTANCE]->(:Substance {name: "Prednisolon", mmiId: 6}),
				(n39)-[:IS_SUBSTANCE]->(n38),
				(:Name {name: "Adrenalin"})-[:REFERENCES]->(:Substance {name: "Epinephrin", mmiId: 4})<-[:IS_SUBSTANCE]-(n43:MmiIngredient:Ingredient {isActive: "true", massFrom: 300, mmiId: 4})<-[:CONTAINS]-(n44:Drug {amount: "0,3", mmiId: 4})<-[:CONTAINS]-(n45:Product {name: "Anapen 300 µg kohlpharma Injektionslösung", mmiId: 4})<-[:BELONGS_TO]-(n58:Package {amount: 1, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "1 st", mmiId: 4, name: "Anapen 300ug", onMarketDate: "2022-07-15"}),
				(n43)-[:HAS_UNIT]->(:Unit:UCUM {print: "μg", ucumCs: "ug", ucumCi: "UG", mmiCode: "MCG", mmiName: "μg"}),
				(n48)<-[:IS_SUBSTANCE]-(n51:MmiIngredient:Ingredient {isActive: "false", massFrom: 3, mmiId: 5})-[:HAS_UNIT]->(n32)<-[:HAS_UNIT]-(n44)-[:HAS_DOSE_FORM]->(:MmiDoseForm {mmiCode: 220, mmiName: "Injektionslsg.", mmiDesc: "parenteral"})-[:CORRESPONDS_TO]->(:EDQM:Code:DoseForm {code: "PDF-11201000", name: "Solution for injection", status: "Current", intendedSite: "Parenteral"}),
				(:Name {name: "Wasser"})-[:REFERENCES]->(n48),
				(n29)-[:CONTAINS]->(n51),
				(:PZN:Code {code: 10000002})-[:REFERENCES]->(:Package {amount: 5, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "5 Ampullen", mmiId: 2, name: "Dormicum 15mg Ampullen", onMarketDate: "2020-01-01"})-[:BELONGS_TO]->(n22),
				(:PZN:Code {code: 10000003})-[:REFERENCES]->(:Package {amount: 5, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "5 Ampullen", mmiId: 3, name: "Dormicum 5mg Ampullen", onMarketDate: "2020-01-01"})-[:BELONGS_TO]->(n33),
				(:PZN:Code {code: 10000004})-[:REFERENCES]->(n58),
				(:PZN:Code {code: 10000005})-[:REFERENCES]->(:Package {amount: 5, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "5 st", mmiId: 5, name: "Anapen 300ug 5 stk.", onMarketDate: "2022-07-15"})-[:BELONGS_TO]->(n45),
				(n50)-[:HAS_UNIT]->(n32)<-[:HAS_UNIT]-(n68:Drug {amount: 2, mmiId: 5})-[:CONTAINS]->(n72),
				(:PZN:Code {code: 10000006})-[:REFERENCES]->(:Package {amount: 1, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "1 l", mmiId: 6, name: "Aseptoderm 1l", onMarketDate: "2007-12-15"})-[:BELONGS_TO]->(:Product {name: "Aseptoderm", mmiId: 5})<-[:MANUFACTURES]-(n65:Company {name: "Dr. Schumacher GmbH", shortName: "Dr. Schumacher GmbH", mmiId: 2}),
				(n65)-[:HAS_ADDRESS]->(:Address {street: "Zum Steger", streetNumber: 3, postalCode: 34323, city: "Malsfeld", country: "Deutschland", countryCode: "DE", mmiId: 2}),
				(:Substance {name: "Prednisolon 21-hydrogensuccinat, Natriumsalz", mmiId: 7})<-[:IS_SUBSTANCE]-(n76)<-[:CONTAINS]-(n70:Drug {amount: 1, mmiId: 6})<-[:CONTAINS]-(n67:Product {name: "Prednisolut® 10 mg L, Pulver und Lösungsmittel zur Herstellung einer Injektionslösung", mmiId: 6})-[:CONTAINS]->(n68)-[:HAS_DOSE_FORM]->(:MmiDoseForm {mmiCode: 432, mmiName: "Lösungsmittel"}),
				(n70)-[:HAS_DOSE_FORM]->(:MmiDoseForm {mmiCode: 432, mmiName: "Pulver zur Herst. e. Inj.-Lsg.", mmiDesc: "parenteral"})-[:CORRESPONDS_TO]->(:EDQM:Code:DoseForm {code: "PDF-11205000", name: "Powder for solution for injection", status: "Current", intendedSite: "Parenteral"}),
				(:PZN:Code {code: 01343446})-[:REFERENCES]->(:Package {amount: 3, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "3 st", mmiId: 7, name: "Prednisolut 10mg L 3 Amp. m. Pulv. + 3 Amp. m. Lsgm. N2", onMarketDate: "1998-12-15"})-[:BELONGS_TO]->(n67),
				(n77)-[:HAS_UNIT]->(n6)
				"""
				.stripIndent()
				.replace("\"true\"", "true") // Arrows.app does not detect booleans...
				.replace("\"false\"", "false")
				.replaceAll("code: ([\\d]+)", "code: \"$1\"") // codes are always strings...
				.replaceAll("mass(From|To): ([\\d]+)", "mass$1: \"$2\"") // massFrom and massTo shall always be strings
				.replaceAll("amount: ([\\d]+)", "amount: \"$1\"") // Same goes for amount
				;
	}

	@Override
	public void close() {
		ConnectionConfiguration.setDefault(originalDefaultConfig);
		getDatabase().close();
		Neo4jDatabaseTestExtension.neo4j = null;
	}
}
