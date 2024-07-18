package de.medizininformatikinitiative.medgraph.graphdbpopulator;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseTools;
import de.medizininformatikinitiative.medgraph.common.mvc.NamedProgressableImpl;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the actions required to perform a graph database population.
 *
 * @author Markus Budeus
 */
public class GraphDbPopulation extends NamedProgressableImpl {

	// No separate test case exists for this class. It is indirectly tested through the integration test.

	@NotNull
	private final Path mmiPharmindexPath;
	@NotNull
	private final Path neo4jImportPath;
	@Nullable
	private final Path amiceFilePath;

	/**
	 * Creates a {@link GraphDbPopulatorSupport}, which is capable of executing the tasks required to fill the Neo4j database
	 * with the MMI Pharmindex data.
	 *
	 * @param mmiPharmindexPath the path to the MMI Pharmindex data to use for loading
	 * @param neo4jImportPath   the path to the Neo4j import directory
	 */
	public GraphDbPopulation(@NotNull Path mmiPharmindexPath, @NotNull Path neo4jImportPath) {
		this(mmiPharmindexPath, neo4jImportPath, null);
	}

	/**
	 * Creates a {@link GraphDbPopulatorSupport}, which is capable of executing the tasks required to fill the Neo4j database
	 * with the MMI Pharmindex data.
	 *
	 * @param mmiPharmindexPath the path to the MMI Pharmindex data to use for loading
	 * @param neo4jImportPath   the path to the Neo4j import directory
	 * @param amiceFilePath     optionally, a path to the AMIce Stoffbezeichnungen Rohdaten file, may be null
	 */
	public GraphDbPopulation(@NotNull Path mmiPharmindexPath, @NotNull Path neo4jImportPath,
	                         @Nullable Path amiceFilePath) {
		this.mmiPharmindexPath = mmiPharmindexPath;
		this.neo4jImportPath = neo4jImportPath;
		this.amiceFilePath = amiceFilePath;
	}

	public void executeDatabasePopulation(DatabaseConnection connection) throws IOException {
		setTaskStack("Preparing data import");
		setProgress(0);

		GraphDbPopulatorSupport graphDbPopulatorSupport = new GraphDbPopulatorSupport();

		graphDbPopulatorSupport.copyKnowledgeGraphSourceDataToNeo4jImportDirectory(
				mmiPharmindexPath,
				amiceFilePath,
				neo4jImportPath
		);

		try (Session session = connection.createSession()) {

			List<Loader> loaders = prepareLoaders(session, amiceFilePath != null);
			setProgress(1, loaders.size() + 3);

			setTaskStack("Clearing database");
			DatabaseTools.clearDatabase(session);

			setProgress(2);
			loaders.forEach(this::runLoader);
		}

		setTaskStack("Cleaning up");
		graphDbPopulatorSupport.removeFilesFromNeo4jImportDir(neo4jImportPath);
		incrementProgress();

		setTaskStack();
	}

	/**
	 * Creates the loaders which take part in setting up the graph database and returns them as a list. Note that the
	 * loaders must be executed in the order given by the list, otherwise dependencies between the loaders may not be
	 * honored, which might cause failures or missing data in the resulting knowledge graph.
	 *
	 * @param session            the session to connect the loaders to
	 * @param includeAmiceLoader if true, includes the {@link AmiceStoffBezLoader} which requires the corresponding file
	 *                           be present
	 * @return a list of loaders, ready for execution
	 */
	private List<Loader> prepareLoaders(Session session, boolean includeAmiceLoader) {
		List<Loader> loaders = new ArrayList<>();

		// Unit nodes
		loaders.add(new UnitLoader(session));
		// EDQM Standard Term nodes
		loaders.add(new EdqmStandardTermsLoader(session));
		// EDQM Standard Term Relations
		loaders.add(new EdqmStandardTermsRelationsLoader(session));
		// MMI Dose forms
		loaders.add(new DoseFormLoader(session));
		// Relations between MMI dose forms and EDQM dose forms
		loaders.add(new MmiEdqmDoseFormConnectionsLoader(session));
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
		if (includeAmiceLoader) loaders.add(new AmiceStoffBezLoader(session));
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
		// Corresponding ingredients and their amounts
		loaders.add(new IngredientCorrespondenceLoader(session));
		// Custom synonymes
		loaders.add(new CustomSynonymeLoader(session));
		// Dose form translations
		loaders.add(new EdqmStandardTermsTranslationsLoader(session));
		// Custom dose form synonymes
		loaders.add(new EdqmStandardTermsCustomSynonymesLoader(session));
		// Coding System Nodes and connections to it
		loaders.add(new CodingSystemNodeCreator(session));
		// Synonymes from other nodes
		loaders.add(new DatabaseSynonymePreparer(session));

		return loaders;
	}

	private void runLoader(Loader loader) {
		String taskName = "Running " + loader.getClass().getSimpleName();
		setTaskStack(taskName);
		loader.setOnSubtaskStartedListener(s -> setTaskStack(taskName, s));
		loader.setOnSubtaskCompletedListener(() -> setTaskStack(taskName));
		loader.execute();
		setProgress(getProgress() + 1);
	}

}
