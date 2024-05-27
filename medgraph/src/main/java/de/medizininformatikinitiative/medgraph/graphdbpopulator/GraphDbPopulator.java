package de.medizininformatikinitiative.medgraph.graphdbpopulator;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders.*;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.neo4j.driver.Values.parameters;

/**
 * @author Markus Budeus
 */
public class GraphDbPopulator {


	private static final String[] REQUIRED_FILES = new String[]{
			"CATALOGENTRY.CSV",
			"COMPANY.CSV",
			"COMPANYADDRESS.CSV",
			"COMPOSITIONELEMENT.CSV",
			"COMPOSITIONELEMENTEQUI.CSV",
			"ITEM.CSV",
			"ITEM_ATC.CSV",
			"ITEM_COMPOSITIONELEMENT.CSV",
			"MOLECULE.CSV",
			"PACKAGE.CSV",
			"PRODUCT.CSV",
			"PRODUCT_COMPANY.CSV",
			"PRODUCT_FLAG.CSV"
	};
	private static final String[] REQUIRED_RESOURCE_FILES = new String[]{
			"amice_stoffbezeichnungen_utf8.csv",
			"custom_synonymes.csv",
			"dose_form_mapping.csv",
			"gsrs_matches.csv",
			"NOTICE.txt",
	};

	private static final String MMI_PHARMINDEX_FILES_SUBPATH = "mmi_pharmindex";

	/**
	 * Uses the given session to run statements against the database which remove all nodes, relationships and
	 * constraints.
	 */
	public void clearDatabase(Session session) {
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
	public List<Loader> prepareLoaders(Session session) {
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

	/**
	 * Attempts to copy the required MMI Pharmindex files from the given path as well as the resource files packaged
	 * with this application to the Neo4j import directory, as specified by the second argument.
	 *
	 * @param mmiPharmindexDirectoryPath the path where the MMI Pharmindex data is stored
	 * @param neo4jImportDirectoryPath   the Neo4j import directory path
	 * @throws IOException              if a file operation failed
	 * @throws IllegalArgumentException if no directory exists at the mmiPharmindexDirectoryPath, it points to something
	 *                                  that is not a directory or not all required MMI Pharmindex files are present in
	 *                                  the mmiPharmindexDirectoryPath, also if the neo4jImportDirectoryPath does not
	 *                                  point to a directory
	 */
	public void copyKnowledgeGraphSourceDataToNeo4jImportDirectory(Path mmiPharmindexDirectoryPath,
	                                                                      Path neo4jImportDirectoryPath)
	throws IOException {
		File mmiPharmindexDir = checkAndGetMmiPharmindexDir(mmiPharmindexDirectoryPath);
		copyRequiredFilesToImportDir(mmiPharmindexDir, neo4jImportDirectoryPath);
	}

	/**
	 * Ensures the given path points to an existing directory and all required MMI Pharmindex files reside in there.
	 *
	 * @param path the path to verify
	 * @return the given path as file
	 * @throws IllegalArgumentException if no directory exists at the given path, the path points to something that is
	 *                                  not a directory or not all required MMI Pharmindex files are present in the
	 *                                  directory
	 */
	private File checkAndGetMmiPharmindexDir(Path path) {
		File dir = path.toFile();
		if (!dir.exists()) {
			throw new IllegalArgumentException("The given directory with the MMI Pharmindex data does not exist!");
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(
					"The path given as MMI Pharmindex data directory does not point to a directory!");
		}

		for (String requiredFile : REQUIRED_FILES) {
			File target = new File(dir.getAbsolutePath() + File.separator + requiredFile);
			if (!target.exists()) {
				throw new IllegalArgumentException(
						"Could not find the file " + requiredFile + " in the MMI Pharmindex data directory!");
			}
		}
		return dir;
	}

	/**
	 * Attempts to copy the required MMI Pharmindex files from the given directory as well as the resource files
	 * packaged with this application to the Neo4j import directory, as specified by the second argument.
	 *
	 * @param sourceDir       the path where the MMI Pharmindex data is stored
	 * @param neo4jImportPath the Neo4j import directory path
	 * @throws IOException              if a file operation failed
	 * @throws IllegalArgumentException if the neo4jImportPath does not point to a directory
	 */
	private void copyRequiredFilesToImportDir(File sourceDir, Path neo4jImportPath) throws IOException {
		File targetDir = neo4jImportPath.toFile();
		if (!targetDir.exists()) {
			throw new IllegalArgumentException("The given Neo4j import directory does not exist!");
		}
		if (!targetDir.isDirectory()) {
			throw new IllegalArgumentException("The given Neo4j import directory is not a directory!");
		}

		File mmiTargetDir = new File(targetDir + File.separator + MMI_PHARMINDEX_FILES_SUBPATH);
		if (!mmiTargetDir.exists())
			if (!mmiTargetDir.mkdir())
				throw new IOException("Failed to create subdirectory in Neo4j import directory");

		// Copy MMI Pharmindex files
		Path target = mmiTargetDir.toPath();
		Path source = sourceDir.toPath();
		for (String file : REQUIRED_FILES) {
			Path original = source.resolve(file);
			Path targetFile = target.resolve(file);
			Files.copy(original, targetFile, StandardCopyOption.REPLACE_EXISTING);
		}

		// Copy other files
		target = targetDir.toPath();
		for (String resource : REQUIRED_RESOURCE_FILES) {
			InputStream stream = Objects.requireNonNull(GraphDbPopulator.class.getResourceAsStream("/" + resource));
			Path targetPath = target.resolve(resource);
			Files.copy(stream, targetPath, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Deletes the files required for the setup from the Neo4j import directory.
	 *
	 * @param neo4jImportPath the path to the Neo4j import directory
	 * @throws IOException if a file operation failed
	 */
	public void removeFilesFromNeo4jImportDir(Path neo4jImportPath) throws IOException {
		Path mmiTargetDir = neo4jImportPath.resolve(MMI_PHARMINDEX_FILES_SUBPATH);

		for (String filename : REQUIRED_FILES) {
			Files.delete(mmiTargetDir.resolve(filename));
		}

		for (String filename : REQUIRED_RESOURCE_FILES) {
			Files.delete(neo4jImportPath.resolve(filename));
		}

		Files.delete(mmiTargetDir);
	}

}