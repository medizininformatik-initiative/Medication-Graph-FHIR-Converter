package de.medizininformatikinitiative.medgraph.searchengine.tracing;

/**
 * @author Markus Budeus
 */
public interface InputUsageStatement <T> {

	// TODO Javadoc

	T getInput();
	T getUnusedParts();

	T getUsedParts();

}
