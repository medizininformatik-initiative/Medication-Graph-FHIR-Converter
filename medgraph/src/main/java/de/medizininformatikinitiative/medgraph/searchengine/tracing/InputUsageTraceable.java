package de.medizininformatikinitiative.medgraph.searchengine.tracing;

/**
 * A traceable object is any result of an operation which can report which parts of the input to the operation were used
 * for the result.
 *
 * @author Markus Budeus
 * @see InputUsageStatement
 */
public interface InputUsageTraceable<T extends InputUsageStatement<?>> {

	/**
	 * Returns the input usage statement for this operation result.
	 */
	T getUsageStatement();

}
