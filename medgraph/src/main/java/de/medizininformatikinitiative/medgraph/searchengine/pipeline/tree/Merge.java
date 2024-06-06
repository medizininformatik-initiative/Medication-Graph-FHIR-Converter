package de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree;

/**
 * A merge is a {@link SplitCause} which indicates the tree is split at this location because the parts it is split
 * into were once different trees that were merged into a single tree.
 *
 * @author Markus Budeus
 */
class Merge implements SplitCause {

	@Override
	public String getName() {
		return "Merge";
	}
}
