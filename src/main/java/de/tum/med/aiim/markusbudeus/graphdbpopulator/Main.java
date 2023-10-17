package de.tum.med.aiim.markusbudeus.graphdbpopulator;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders.*;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		runMigrators(true);
	}

	public static void runMigrators(boolean includeInn) throws IOException, InterruptedException {
		try (DatabaseConnection connection = new DatabaseConnection();
		     Session session = connection.createSession()) {

			long time = System.currentTimeMillis();

			List<Loader> loaders = new ArrayList<>();

			// Unit nodes and UCUM definitions
			loaders.add(new UnitLoader(session));
			// MMI Dose forms
			loaders.add(new DoseFormLoader(session));
			// EDQM Dose forms
			loaders.add(new EdqmDoseFormLoader(session));
			// Substance nodes, ASK nodes and CAS nodes and their relations
			loaders.add(new SubstanceLoader(session));
			// Product nodes
			loaders.add(new ProductLoader(session));
			// PZN nodes and their relations with Product nodes
			loaders.add(new PznLoader(session));
			if (includeInn) {
				// INN nodes and relations to CAS nodes
				loaders.add(new InnLoader(session));
			}
			// Manufacturer nodes
			loaders.add(new CompanyLoader(session));
			// Relation between Manufacturer nodes and their product nodes
			loaders.add(new CompanyProductReferenceLoader(session));
			// Drug nodes and relations to Product nodes
			loaders.add(new DrugLoader(session));
			// Ingredient nodes and relations to Substance nodes
			loaders.add(new IngredientLoader(session));
			// Relations between Ingredient nodes and Drug nodes
			loaders.add(new DrugIngredientConnectionLoader(session));

			for (Loader loader : loaders) {
				loader.execute();
			}
			System.out.println("Took " + (System.currentTimeMillis() - time) + "ms");

		}
	}

}