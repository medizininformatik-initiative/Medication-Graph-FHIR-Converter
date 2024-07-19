package de.medizininformatikinitiative.medgraph;

import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration;
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfigurationService;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionService;
import org.junit.jupiter.api.extension.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test extension which provides a local JVM Neo4j database instance for testing. Test classes can use
 * {@link ExtendWith} to include this extension, allowing them to access the Neo4j instance using
 * {@link #getDatabase()}. An easier way is to extend {@link Neo4jTest}, which is already correctly configured. Please
 * note your test case must subclass {@link UnitTest}, as its functionality for inserting mock dependencies is used.
 * <p>
 * For each test, the {@link ConnectionConfigurationService} and {@link DatabaseConnectionService} dependencies are set
 * to mock instances which point to the test database. However, you can override that by inserting your own dependencies
 * in your tests or in a {@link org.junit.jupiter.api.BeforeEach BeforeEach} method.
 * <p>
 * The database is shared among all test classes using it and not reset between tests or test classes in any way. Write
 * access to the database is therefore highly discouraged.
 *
 * @author Markus Budeus
 * @see Neo4jTest
 */
public class Neo4jDatabaseTestExtension implements BeforeAllCallback, BeforeEachCallback, ExtensionContext.Store.CloseableResource {

	private static volatile Neo4j neo4j;
	private static volatile DatabaseConnection connection;

	private static volatile ConnectionConfiguration connectionConfiguration;

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

	/**
	 * Sets up the {@link DatabaseConnectionService} and {@link ConnectionConfigurationService} dependencies to use the
	 * test Neo4j database.
	 */
	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		UnitTest unitTest;
		try {
			unitTest = (UnitTest) context.getRequiredTestInstance();
		} catch (ClassCastException e) {
			// Yes, I could also directly inject dependencies. However, in case the implementing test does not extend
			// UnitTest, these dependencies will not be reset after the test and may spill into other tests, which
			// can cause unexpected and hard-to-diagnose issues. By requiring the use of UnitTest, I assume the user
			// is informed about the dependency injection mocking policy.
			throw new UnsupportedOperationException("Your test class must extend UnitTest, as its dependency injection " +
					"overriding mechanism is used!");
		}
		ConnectionConfigurationService connectionConfigurationService = mock();
		DatabaseConnectionService databaseConnectionService = mock();

		when(connectionConfigurationService.getConnectionConfiguration()).thenReturn(connectionConfiguration);
		when(databaseConnectionService.createConnection()).thenAnswer(
				req -> connectionConfiguration.createConnection());

		unitTest.insertMockDependency(ConnectionConfigurationService.class, connectionConfigurationService);
		unitTest.insertMockDependency(DatabaseConnectionService.class, databaseConnectionService);
	}

	private static void initializeDatabase() {
		Neo4jDatabaseTestExtension.neo4j = Neo4jBuilders.newInProcessBuilder()
		                                                .withFixture(getDatabaseFixture())
		                                                .build();

		connectionConfiguration = new ConnectionConfiguration(
				neo4j.boltURI().toString(),
				"neo4j",
				"neo4j".toCharArray()
		);

		Neo4jDatabaseTestExtension.connection = connectionConfiguration.createConnection();
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
				CREATE (n81:EDQM:Code {code: "RCA-0047", name: "Conventional", status: "Current", type: "Release characteristic"})<-[:HAS_CHARACTERISTIC]-(n5:EDQM:Code:DoseForm {code: "PDF-10204000", name: "Granules", status: "Current", type: "Pharmaceutical dose form", german: "Granulat"})<-[:CORRESPONDS_TO]-(:MmiDoseForm {mmiCode: 20, mmiName: "Gran. zum Einnehmen", mmiDesc: "Zum Einnehmen"})<-[:HAS_DOSE_FORM]-(n15:Drug {amount: 1, mmiId: 1})-[:CONTAINS]->(n1:MmiIngredient:Ingredient {isActive: "true", massFrom: 500, mmiId: 1})-[:IS_SUBSTANCE]->(:Substance {name: "Acetylsalicylsäure", mmiId: 1})<-[:REFERENCES]-(:Synonyme {name: "Aspirin"}),
				(:Address {street: "Kaiser-Wilhelm-Allee", streetNumber: 56, postalCode: 51368, city: "Leverkusen", country: "Deutschland", countryCode: "DE", mmiId: 1})<-[:HAS_ADDRESS]-(:Company {name: "Bayer Vital GmbH", shortName: "Bayer Vital", mmiId: 1})-[:MANUFACTURES]->(n0:Product {name: "Aspirin Complex Granulat-Sticks 500 mg/30 mg Granulat", mmiId: 1})-[:CONTAINS]->(n15),
				(n1)-[:HAS_UNIT]->(n6:Unit:UCUM {print: "mg", ucumCs: "mg", ucumCi: "MG", mmiCode: "MG", mmiName: "mg", name: "mg"})<-[:HAS_UNIT]-(n23:MmiIngredient:Ingredient {isActive: "true", massFrom: "16,68", mmiId: 2})-[:CORRESPONDS_TO]->(n37:Ingredient {massFrom: 15})-[:IS_SUBSTANCE]->(n38:Substance {name: "Midazolam", mmiId: 2})<-[:REFERENCES]-(:Synonyme {name: "Midazolam"}),
				(:Synonyme {name: "Aspirin Complex Granulat-Sticks"})-[:REFERENCES]->(n0)<-[:BELONGS_TO]-(:Package {amount: 5, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "5 st", mmiId: 1, name: "Aspirin Complex 5 Granulat-Sticks", onMarketDate: "2014-07-15"})<-[:REFERENCES]-(:PZN:Code {code: 10000001}),
				(n82:EDQM:Code {code: "BDF-0083", name: "Solution", status: "Current", type: "Basic dose form"})<-[:HAS_CHARACTERISTIC]-(n46:EDQM:Code:DoseForm {code: "PDF-11201000", name: "Solution for injection", status: "Current", type: "Pharmaceutical dose form", german: "Injektionslösung"})-[:HAS_CHARACTERISTIC]->(n80:EDQM:Code {code: "ISI-0033", name: "Parenteral", status: "Current", type: "Intended site"})<-[:HAS_CHARACTERISTIC]-(n26:EDQM:Code:DoseForm {code: "PDF-50060000", name: "Solution for injection/infusion", status: "Current", type: "Pharmaceutical dose form", german: "Injektions-/Infusionslösung"})<-[:CORRESPONDS_TO]-(n30:MmiDoseForm {mmiCode: 518, mmiName: "Injektions-/Infusionslsg.", mmiDesc: "parenteral"})<-[:HAS_DOSE_FORM]-(n29:Drug {amount: 3, mmiId: 2})-[:CONTAINS]->(n23)-[:IS_SUBSTANCE]->(:Substance {name: "Midazolam hydrochlorid", mmiId: 3})<-[:IS_SUBSTANCE]-(n34:MmiIngredient:Ingredient {isActive: "true", massFrom: "5,5", massTo: "5,7", mmiId: 3})-[:HAS_UNIT]->(n6)<-[:HAS_UNIT]-(n37),
				(n30)<-[:HAS_DOSE_FORM]-(n36:Drug {amount: 5, mmiId: 3})<-[:CONTAINS]-(n33:Product {name: "Dormicum® V 5 mg/5 ml Injektionslösung", mmiId: 3})<-[:REFERENCES]-(:Synonyme {name: "Dormicum"})-[:REFERENCES]->(n22:Product {name: "Dormicum® 15 mg/3 ml Injektionslösung", mmiId: 2})-[:CONTAINS]->(n29)-[:HAS_UNIT]->(n32:Unit:UCUM {print: "ml", ucumCs: "ml", ucumCi: "ML", mmiCode: "ML", mmiName: "ml", name: "ml"})<-[:HAS_UNIT]-(n36)-[:CONTAINS]->(n50:MmiIngredient:Ingredient {isActive: "false", massFrom: 5, mmiId: 6})-[:IS_SUBSTANCE]->(n48:Substance {name: "Wasser für Injektionszwecke", mmiId: 5})<-[:IS_SUBSTANCE]-(n72:MmiIngredient:Ingredient {isActive: "false", mmiId: 7}),
				(n36)-[:CONTAINS]->(n34)-[:CORRESPONDS_TO]->(n39:Ingredient {massFrom: 5})-[:HAS_UNIT]->(n6)<-[:HAS_UNIT]-(n76:MmiIngredient:Ingredient {isActive: "true", mmiId: 8, massFrom: "10,48"})-[:CORRESPONDS_TO]->(n77:Ingredient {massFrom: "7,83"})-[:IS_SUBSTANCE]->(:Substance {name: "Prednisolon", mmiId: 6}),
				(n39)-[:IS_SUBSTANCE]->(n38),
				(:Synonyme {name: "Adrenalin"})-[:REFERENCES]->(:Substance {name: "Epinephrin", mmiId: 4})<-[:IS_SUBSTANCE]-(n43:MmiIngredient:Ingredient {isActive: "true", massFrom: 300, mmiId: 4})<-[:CONTAINS]-(n44:Drug {amount: "0,3", mmiId: 4})<-[:CONTAINS]-(n45:Product {name: "Anapen 300 µg kohlpharma Injektionslösung", mmiId: 4})<-[:BELONGS_TO]-(n58:Package {amount: 1, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "1 st", mmiId: 4, name: "Anapen 300ug", onMarketDate: "2022-07-15"}),
				(n43)-[:HAS_UNIT]->(:Unit:UCUM {print: "μg", ucumCs: "ug", ucumCi: "UG", mmiCode: "MCG", mmiName: "μg", name: "ug"}),
				(n48)<-[:IS_SUBSTANCE]-(n51:MmiIngredient:Ingredient {isActive: "false", massFrom: 3, mmiId: 5})-[:HAS_UNIT]->(n32)<-[:HAS_UNIT]-(n44)-[:HAS_DOSE_FORM]->(:MmiDoseForm {mmiCode: 220, mmiName: "Injektionslsg.", mmiDesc: "parenteral"})-[:CORRESPONDS_TO]->(n46)-[:HAS_CHARACTERISTIC]->(n81)<-[:HAS_CHARACTERISTIC]-(n75:EDQM:Code:DoseForm {code: "PDF-11205000", name: "Powder for solution for injection", status: "Current", type: "Pharmaceutical dose form", german: "Pulver zur Herstellung einer Injektionslösung"})-[:HAS_CHARACTERISTIC]->(:EDQM:Code {code: "BDF-0066", name: "Powder", status: "Current", type: "Basic dose form"}),
				(:Synonyme {name: "Wasser"})-[:REFERENCES]->(n48),
				(n29)-[:CONTAINS]->(n51),
				(:PZN:Code {code: 10000002})-[:REFERENCES]->(:Package {amount: 5, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "5 Ampullen", mmiId: 2, name: "Dormicum 15mg Ampullen", onMarketDate: "2020-01-01"})-[:BELONGS_TO]->(n22),
				(:PZN:Code {code: 10000003})-[:REFERENCES]->(:Package {amount: 5, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "5 Ampullen", mmiId: 3, name: "Dormicum 5mg Ampullen", onMarketDate: "2020-01-01"})-[:BELONGS_TO]->(n33),
				(:PZN:Code {code: 10000004})-[:REFERENCES]->(n58),
				(:PZN:Code {code: 10000005})-[:REFERENCES]->(:Package {amount: 5, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "5 st", mmiId: 5, name: "Anapen 300ug 5 stk.", onMarketDate: "2022-07-15"})-[:BELONGS_TO]->(n45),
				(n50)-[:HAS_UNIT]->(n32)<-[:HAS_UNIT]-(n68:Drug {amount: 2, mmiId: 5})-[:CONTAINS]->(n72),
				(:PZN:Code {code: 10000006})-[:REFERENCES]->(:Package {amount: 1, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "1 l", mmiId: 6, name: "Aseptoderm 1l", onMarketDate: "2007-12-15"})-[:BELONGS_TO]->(:Product {name: "Aseptoderm", mmiId: 5})<-[:MANUFACTURES]-(n65:Company {name: "Dr. Schumacher GmbH", shortName: "Dr. Schumacher GmbH", mmiId: 2}),
				(n65)-[:HAS_ADDRESS]->(:Address {street: "Zum Steger", streetNumber: 3, postalCode: 34323, city: "Malsfeld", country: "Deutschland", countryCode: "DE", mmiId: 2}),
				(:Substance {name: "Prednisolon 21-hydrogensuccinat, Natriumsalz", mmiId: 7})<-[:IS_SUBSTANCE]-(n76)<-[:CONTAINS]-(n70:Drug {amount: 1, mmiId: 6})<-[:CONTAINS]-(n67:Product {name: "Prednisolut® 10 mg L, Pulver und Lösungsmittel zur Herstellung einer Injektionslösung", mmiId: 6})-[:CONTAINS]->(n68)-[:HAS_DOSE_FORM]->(:MmiDoseForm {mmiCode: 432, mmiName: "Lösungsmittel"}),
				(n70)-[:HAS_DOSE_FORM]->(:MmiDoseForm {mmiCode: 432, mmiName: "Pulver zur Herst. e. Inj.-Lsg.", mmiDesc: "parenteral"})-[:CORRESPONDS_TO]->(n75)-[:HAS_CHARACTERISTIC]->(n80)<-[:REFERENCES]-(:Synonyme {name: "Parenteral"}),
				(:PZN:Code {code: 01343446})-[:REFERENCES]->(:Package {amount: 3, `amountFactor1`: 1, `amountFactor2`: 1, amountText: "3 st", mmiId: 7, name: "Prednisolut 10mg L 3 Amp. m. Pulv. + 3 Amp. m. Lsgm. N2", onMarketDate: "1998-12-15"})-[:BELONGS_TO]->(n67),
				(n77)-[:HAS_UNIT]->(n6),
				(n82)<-[:HAS_CHARACTERISTIC]-(n26)-[:HAS_CHARACTERISTIC]->(n81),
				(:Synonyme {name: "Oral"})-[:REFERENCES]->(:EDQM:Code {code: "ISI-0031", name: "Oral", status: "Current", type: "Intended site"})<-[:HAS_CHARACTERISTIC]-(n5)-[:HAS_CHARACTERISTIC]->(:EDQM:Code {code: "BDF-0053", name: "Granules", status: "Current", type: "Basic dose form"})<-[:REFERENCES]-(:Synonyme {name: "Granules"}),
				(:Synonyme {name: "Aspirin Sticks"})-[:REFERENCES]->(n0),
				(:Synonyme {name: "Injektionslsg."})-[:REFERENCES]->(n46)<-[:REFERENCES]-(:Synonyme {name: "Inj.-Lsg."}),
				(:Synonyme {name: "Granules"})-[:REFERENCES]->(n5)
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
		getDatabase().close();
		Neo4jDatabaseTestExtension.neo4j = null;
	}
}
