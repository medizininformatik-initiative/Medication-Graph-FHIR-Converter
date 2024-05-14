package de.medizininformatikinitiative.medgraph.searchengine.algorithm.refining;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree.SubSortingTree;

import java.util.List;

/**
 * @author Markus Budeus
 */
public class ExperimentalRefiner implements MatchRefiner {

	@Override
	public SubSortingTree<MatchingObject> refineMatches(List<? extends MatchingObject> initialMatches,
	                                                    SearchQuery query) {
		return null;
	}

}
