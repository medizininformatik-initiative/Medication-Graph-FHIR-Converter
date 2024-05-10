package de.medizininformatikinitiative.medgraph.searchengine.pipeline;

import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree.SubSortingTree;

import java.util.List;

/**
 * @author Markus Budeus
 */
public class OngoingMatching {

	private SubSortingTree<MatchingObject> currentMatches;

	public OngoingMatching(List<OriginalMatch> matchList) {
		currentMatches = new SubSortingTree<>(matchList);
	}



}
