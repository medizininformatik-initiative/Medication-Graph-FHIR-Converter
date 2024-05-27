package de.medizininformatikinitiative.medgraph.graphdbpopulator;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders.*;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

/**
 * @author Markus Budeus
 */
public class GraphDbPopulator {

	public static void main(String[] args) {
		DatabaseConnection.runSession(session -> {
			Result result = session.run("SHOW CONSTRAINTS YIELD name");
			result.stream().forEach(System.out::println);
		});
	}

	/**
	 * Uses the given session to run statements against the database which remove all nodes, relationships and
	 * constraints.
	 */
	public static void clearDatabase(Session session) {
		// Drop relationships
		session.run("MATCH ()-[r]->() CALL { WITH r DELETE r } IN TRANSACTIONS OF 10000 ROWS");
		// Drop nodes
		session.run("MATCH (n) CALL { WITH n DETACH DELETE n } IN TRANSACTIONS OF 10000 ROWS");
		// Load constraints
		Result constraints = session.run("SHOW CONSTRAINTS YIELD name");
		List<String> constraintNames = new ArrayList<>();
		constraints.forEachRemaining(record -> constraintNames.add(record.get("name", (String) null)));
		// Drop constraints
		constraintNames.forEach(name -> {
			session.run(new Query("DROP CONSTRAINT $constraint", parameters("constraint", name)));
		});
	}

	/**
	 * Creates the loaders which take part in setting up the graph database and returns them as a list. Note that the
	 * loaders must be executed in the order given by the list, otherwise dependencies between the loaders may not be
	 * honored, which might cause failures or missing data in the resulting knowledge graph.
	 *
	 * @param session the session to connect the loaders to
	 * @return a list of loaders, ready for execution
	 */
	public static List<Loader> prepareLoaders(Session session) {
		List<Loader> loaders = new ArrayList<>();

		// Unit nodes
		loaders.add(new UnitLoader(session));
		// MMI Dose forms
		loaders.add(new DoseFormLoader(session));
		// EDQM Dose forms
		loaders.add(new EdqmDoseFormLoader(session));
		// ATC Hierarchy
		loaders.add(new AtcLoader(session));
		// Substance nodes, ASK nodes and CAS nodes and their relations
		loaders.add(new SubstanceLoader(session));
		// Product nodes
		loaders.add(new ProductLoader(session));
		// Delete all products which are not pharmaceuticals
		loaders.add(new ProductFilter(session));
		// Package nodes and their relations with Product nodes
		loaders.add(new PackageLoader(session));
		// INN and CAS nodes
		loaders.add(new AmiceStoffBezLoader(session));
		// Manufacturer nodes
		loaders.add(new CompanyLoader(session));
		// Manufacturer Address nodes
		loaders.add(new CompanyAddressLoader(session));
		// Relation between Manufacturer nodes and their product nodes
		loaders.add(new CompanyProductReferenceLoader(session));
		// Drug nodes and relations to Product nodes
		loaders.add(new DrugLoader(session));
		// Ingredient nodes and relations to Substance nodes
		loaders.add(new IngredientLoader(session));
		// Relations between Ingredient nodes and Drug nodes
		loaders.add(new DrugIngredientConnectionLoader(session));
		// Relations between Drug nodes and ATC nodes
		loaders.add(new DrugAtcConnectionLoader(session));
		// Unit UCUM definitions
		loaders.add(new UcumLoader(session));
		// GSRS UNIIs, RXCUIs, etc.
		loaders.add(new UniiLoader(session));
		// Custom synonymes
		loaders.add(new CustomSynonymeLoader(session));
		// Corresponding ingredients and their amounts
		loaders.add(new IngredientCorrespondenceLoader(session));
		// Coding System Nodes and connections to it
		loaders.add(new CodingSystemNodeCreator(session));
		// Synonymes from other nodes
		loaders.add(new DatabaseSynonymePreparer(session));

		return loaders;
	}

}
