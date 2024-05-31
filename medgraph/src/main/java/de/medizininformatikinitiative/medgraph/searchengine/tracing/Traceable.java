package de.medizininformatikinitiative.medgraph.searchengine.tracing;

/**
 * @author Markus Budeus
 */
public interface Traceable<T extends InputUsageStatement<?>> {

	// TODO Javadoc
	T getUsageStatement();

}
