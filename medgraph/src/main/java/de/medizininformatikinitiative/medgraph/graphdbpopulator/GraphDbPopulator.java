package de.medizininformatikinitiative.medgraph.graphdbpopulator;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders.*;
import de.medizininformatikinitiative.medgraph.searchengine.tools.DatabaseTools;
import org.neo4j.driver.Session;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Markus Budeus
 */
public class GraphDbPopulator {

	/**
	 * If a line in a CSV file starts with this string, it is considered a comment.
	 */
	public static final String CSV_COMMENT_INDICATOR = "#";

	private static final String[] REQUIRED_MMI_FILES = new String[]{
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
			"custom_synonymes.csv",
			"dose_form_mapping.csv",
			"gsrs_matches.csv",
			"edqm_objects.csv",
			"pdf_relations.csv",
			"edqm_translations.csv",
			"dose_form_synonymes.csv",
			"NOTICE.txt",
	};
	private static final Path[] OPTIONAL_FILES = new Path[]{
			AmiceStoffBezLoader.RAW_DATA_FILE_PATH,
	};

	private static final String MMI_PHARMINDEX_FILES_SUBPATH = "mmi_pharmindex";

	/**
	 * Uses the given session to run statements against the database which remove all nodes, relationships and
	 * constraints.
	 */
	public void clearDatabase(Session session) {
		DatabaseTools.clearDatabase(session);
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
	public List<Loader> prepareLoaders(Session session, boolean includeAmiceLoader) {
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

	/**
	 * Attempts to copy the required MMI Pharmindex files from the given path as well as the resource files packaged
	 * with this application to the Neo4j import directory, as specified by the second argument.
	 *
	 * @param mmiPharmindexDirectoryPath the path where the MMI Pharmindex data is stored
	 * @param amiceDataFilePath          the path where the AMIce Stoffbezeichnungen Rohdaten file is at, may be null
	 * @param neo4jImportDirectoryPath   the Neo4j import directory path
	 * @throws IOException              if a file operation failed
	 * @throws IllegalArgumentException if no directory exists at the mmiPharmindexDirectoryPath, it points to something
	 *                                  that is not a directory or not all required MMI Pharmindex files are present in
	 *                                  the mmiPharmindexDirectoryPath, also if the neo4jImportDirectoryPath does not
	 *                                  point to a directory
	 */
	public void copyKnowledgeGraphSourceDataToNeo4jImportDirectory(
			Path mmiPharmindexDirectoryPath,
			Path amiceDataFilePath,
			Path neo4jImportDirectoryPath)
	throws IOException {
		copyRequiredFilesToImportDir(
				checkAndGetMmiPharmindexDir(mmiPharmindexDirectoryPath),
				checkAmiceFilePath(amiceDataFilePath),
				neo4jImportDirectoryPath);
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

		for (String requiredFile : REQUIRED_MMI_FILES) {
			File target = new File(dir.getAbsolutePath() + File.separator + requiredFile);
			if (!target.exists()) {
				throw new IllegalArgumentException(
						"Could not find the file " + requiredFile + " in the MMI Pharmindex data directory!");
			}
		}
		return dir;
	}

	private File checkAmiceFilePath(Path path) {
		if (path == null) return null;
		File f = path.toFile();
		if (!f.isFile()) {
			throw new IllegalArgumentException("The given path for the AMIce data file does not point to a file!");
		}
		return f;
	}

	/**
	 * Attempts to copy the required MMI Pharmindex files from the given directory as well as the resource files
	 * packaged with this application to the Neo4j import directory, as specified by the second argument.
	 *
	 * @param mmiSourceDir    the path where the MMI Pharmindex data is stored
	 * @param amiceSourceFile the amice data source file or null if it is not to be used
	 * @param neo4jImportPath the Neo4j import directory path
	 * @throws IOException              if a file operation failed
	 * @throws IllegalArgumentException if the neo4jImportPath does not point to a directory
	 */
	private void copyRequiredFilesToImportDir(File mmiSourceDir, File amiceSourceFile, Path neo4jImportPath)
	throws IOException {
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
		Path source = mmiSourceDir.toPath();
		for (String file : REQUIRED_MMI_FILES) {
			Path original = source.resolve(file);
			Path targetFile = target.resolve(file);
			Files.copy(original, targetFile, StandardCopyOption.REPLACE_EXISTING);
		}

		// Copy AMIce file
		if (amiceSourceFile != null) {
			Path targetAmicePath = neo4jImportPath.resolve(AmiceStoffBezLoader.RAW_DATA_FILE_PATH);
			try (InputStream stream = new FileInputStream(amiceSourceFile)) {
				copyAmiceFileAndFixBrokenSynonyms(stream, targetAmicePath);
			}
		}

		// Copy other files
		target = targetDir.toPath();
		for (String resource : REQUIRED_RESOURCE_FILES) {
			try(InputStream stream = Objects.requireNonNull(GraphDbPopulator.class.getResourceAsStream("/" + resource))) {
				Path targetPath = target.resolve(resource);
				copyCsvAndStripComments(stream, targetPath);
			}
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

		for (String filename : REQUIRED_MMI_FILES) {
			Files.delete(mmiTargetDir.resolve(filename));
		}

		for (String filename : REQUIRED_RESOURCE_FILES) {
			Files.delete(neo4jImportPath.resolve(filename));
		}

		for (Path path : OPTIONAL_FILES) {
			try {
				Files.delete(neo4jImportPath.resolve(path));
			} catch (NoSuchFileException ignored) {
			}
		}

		Files.delete(mmiTargetDir);
	}

	/**
	 * Copies CSV data from the given input stream to the target path, overwriting any file which may reside there. CSV
	 * Comments (lines starting with {@link #CSV_COMMENT_INDICATOR} are stripped.
	 *
	 * @param from the input stream to copy from
	 * @param to   the path to copy the CSV to
	 * @throws IOException if an I/O operation failed
	 */
	private static void copyCsvAndStripComments(InputStream from, Path to) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(from));
		     BufferedWriter writer = new BufferedWriter(new FileWriter(to.toFile()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith(CSV_COMMENT_INDICATOR)) writer.write(line + '\n');
			}
		}
	}

	/**
	 * Copies the CSV data from the given input stream to the given target path. Assumes the semicolon is the separator
	 * sign and checks the last entry in each row for misplaced double quotes.
	 */
	private static void copyAmiceFileAndFixBrokenSynonyms(InputStream from, Path to) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(from));
		     BufferedWriter writer = new BufferedWriter(new FileWriter(to.toFile()))) {
			String line;
			List<Integer> updatedLines = new ArrayList<>();
			int l = 0;
			while ((line = reader.readLine()) != null) {
				l++;
				int lastSplitterIndex = -1;
				for (int i = line.length() - 2; i >= 0;i--) {
					if (line.charAt(i) == ';' && line.charAt(i-1) == '"' && line.charAt(i+1) == '"') {
						lastSplitterIndex = i;
						break;
					}
				}
				if (lastSplitterIndex != -1) {
					String lastPart = line.substring(lastSplitterIndex + 1);
					if (lastPart.startsWith("\"") && lastPart.endsWith("\"")) {
						String newPart = "\"" + lastPart.substring(1, lastPart.length() - 1).replaceAll("\"", "''") +"\"";
						if (!newPart.equals(lastPart))
							updatedLines.add(l);
						lastPart = newPart;
					}
					writer.write(line.substring(0, lastSplitterIndex + 1));
					writer.write(lastPart);
				} else {
					writer.write(line);
				}
				writer.write("\n");
			}
			System.out.println("Updated lines "+updatedLines);
		}
	}

}
