package de.medizininformatikinitiative.medgraph.graphdbpopulator;

import java.nio.file.Path;

/**
 * Factory for {@link GraphDbPopulation}-instances.
 *
 * @author Markus Budeus
 */
@FunctionalInterface
public interface GraphDbPopulationFactory {

	/**
	 * Creates a {@link GraphDbPopulation}-instance which can be used to run the graph database population chain.
	 *
	 * @param mmiPharmindexDirectoryPath the path where the MMI Pharmindex files can be found
	 * @param neo4jImportDirectoryPath   the Neo4j import directory to copy the required files to
	 * @param amiceDataFilePath          optionally, a path to the AMIce Stoffbezeichnungen Rohdaten file, may be null
	 * @return a ready-for-use {@link GraphDbPopulation}-instance
	 */
	GraphDbPopulation prepareDatabasePopulation(Path mmiPharmindexDirectoryPath,
	                                            Path neo4jImportDirectoryPath,
	                                            Path amiceDataFilePath);

}
