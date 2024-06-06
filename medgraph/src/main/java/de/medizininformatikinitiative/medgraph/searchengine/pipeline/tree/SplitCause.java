package de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree;

/**
 * A reason why a {@link SubSortingTree} is split at a specific level.
 *
 * @author Markus Budeus
 */
interface SplitCause {

	/**
	 * Returns the name of the reason why the tree is split at this position.
	 */
	String getName();

}
