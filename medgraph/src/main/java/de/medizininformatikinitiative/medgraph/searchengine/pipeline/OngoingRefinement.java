package de.medizininformatikinitiative.medgraph.searchengine.pipeline;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.*;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Filtering;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Judgement;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Transformation;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.Judge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer.IMatchTransformer;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree.BinarySortDirective;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree.ScoreSortDirective;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree.SubSortingTree;

import java.util.*;

/**
 * This class manages intermediate matching results during a run of the matching algorithm. It exposes methods which can
 * be used to apply matching pipeline steps to the intermediate results, thereby executing parts of the matching
 * algorithm.
 *
 * @author Markus Budeus
 */
public class OngoingRefinement {

	private final SubSortingTree<MatchingObject> currentMatches;
	private final SearchQuery query;

	public OngoingRefinement(List<? extends MatchingObject> matchList, SearchQuery query) {
		this(new SubSortingTree<>(matchList), query);
	}

	/**
	 * Creates a new {@link OngoingRefinement}-instance which manages the given tree.
	 * @param matchList the {@link SubSortingTree} to manage
	 * @param query the query to use for refinements
	 */
	public OngoingRefinement(SubSortingTree<MatchingObject> matchList, SearchQuery query) {
		currentMatches = matchList;
		this.query = query;
	}

	/**
	 * Sorts the current matches using the given {@link ScoreJudge}. Non-passing matches are eliminated from the
	 * matching process.
	 *
	 * @param judge          the judge to use
	 * @param ensureSurvival if this is true, it prevents the elimination of all matches in case no match passes the
	 *                       judgement; instead, nothing gets eliminated, although the failure to pass the judgement is
	 *                       still documented
	 */
	public void applyScoreJudge(ScoreJudge judge, boolean ensureSurvival) {
		boolean anyPass = judgeMatches(judge);
		Double retainThreshold = (anyPass || !ensureSurvival) ? judge.getPassingScore() : null;
		sortMatchesByLatestScoredJudgement(judge.toString(), retainThreshold);
	}

	/**
	 * Filters the current matches using the given filter.
	 *
	 * @param filter         the filter which to use
	 * @param ensureSurvival if this is true, it prevents the elimination of all matches in case no match passes the
	 *                       filter; instead, nothing gets eliminated, although the failure to pass the filter is still
	 *                       documented
	 * @return true if any matches passed the filter, false if none passed the filter (regardless of whether they were
	 * eliminated based on the ensureSurvival parameter)
	 */
	public boolean applyFilter(Judge<? extends Filtering> filter, boolean ensureSurvival) {
		boolean anyPass = judgeMatches(filter);
		if (anyPass || !ensureSurvival) {
			removeMatchesWhichFailedTheLastFiltering(filter.toString());
		}
		return anyPass;
	}

	public void transformMatches(IMatchTransformer transformer) {
		// This function is sadly a lot more complicated than applying a judgement.
		//
		// First, we feed all current matches into the transformer.
		// Then, we construct new TransformedObject-instances for every output of each transformation.
		// Now, intuitively, the final step is to replace each MatchingObject with its transformations using
		// SubSortingTree.batchReplace.
		//
		// However, there is one issue: The MatchTransformer may have generated the same output within the
		// transformations of different inputs. For example, say we transform substances into products by searching
		// for products which use the substance as their active ingredient. But, product [A] may be a combination
		// product, containing substances [B] and [C]. Both [B] and [C] are then resolved to product [A].
		// Now, [A] exists twice in the resulting matches. Which means, we need to merge them.
		//
		// The way we do that is we group all TransformedObject-instances by their Matchable in a hash table.
		// Then, everytime a Matchable is linked to multiple TransformedObject-instances, we merge them.
		// Finally, we do the batchReplace. For groups, we replace the first occurrence on the matches (i.e. the
		// highest-rated) with the Merge object and eliminate the others.

		// Step 1: Run the transformer
		List<MatchingObject> matchingObjects = getCurrentMatches();
		List<Matchable> matchables = matchingObjects.stream().map(MatchingObject::getObject).toList();
		List<Transformation> transformations = transformer.batchTransform(matchables, query);

		// Verify we got the expected amount of transformations
		int size = matchables.size();
		if (size != transformations.size()) {
			throw new IllegalStateException(
					"The transformer was given " + matchables.size() + " objects to transform, " +
							"but returned " + transformations.size() + " transformations!");
		}

		// Step 2: create TransformedObject-instances and group by outcome Matchable
		// Use LinkedHashMap to ensure order to objects in transformation output is preserved.
		Map<Matchable, LinkedList<TransformedObject>> postTransformationMap = new LinkedHashMap<>();
		for (int i = 0; i < size; i++) {
			MatchingObject sourceObject = matchingObjects.get(i);
			Transformation transformation = transformations.get(i);
			for (Matchable output : transformation.getResult()) {
				TransformedObject transformedObject = new TransformedObject(output, sourceObject, transformation);
				// Add to the list of TransformedObjects which represent this Matchable, creating the list if required
				postTransformationMap.computeIfAbsent(output, m -> new LinkedList<>()).add(transformedObject);
			}
		}

		// Step 3: Merge all TransformedObjects which reference the same Matchable.
		// While doing that, prepare the replacement map for the SubSortingTree.
		Map<MatchingObject, List<MatchingObject>> replacementMap = new HashMap<>();
		for (MatchingObject sourceObject : matchingObjects) {
			replacementMap.put(sourceObject, new LinkedList<>());
		}
		for (LinkedList<TransformedObject> list : postTransformationMap.values()) {
			MatchingObject result;
			assert !list.isEmpty();
			if (list.size() == 1) {
				result = list.getFirst();
			} else {
				result = new Merge(list);
			}
			// The remappingKey is the object in the SubSortingTree which is to be replaced with the result.
			// In case of a single-entry list, this is trivial. We simply replace the source object with the
			// transformed one.
			// For a merge, we don't want the Merge to be inserted multiple times, once for each of the merged objects.
			// Instead we use the position closest to the beginning of the list (i.e. the highest-rated position.)
			// The object at this position is the source object of the first transformed object in the list, because
			// the processing happened in this order. So elements further back in the original list are processed later
			// and therefore end up at later positions in the list of transformed objects.
			MatchingObject remappingKey = list.getFirst().getSourceObject();

			replacementMap.get(remappingKey).add(result);
		}

		// Finally, replace MatchingObjects in the SubSortingTree by their TransformedObject or Merge object
		currentMatches.batchReplace(replacementMap);
	}

	/**
	 * Runs the given judge against all current matches and assigns its judgement to the corresponding
	 * {@link MatchingObject}s.
	 *
	 * @param judge the judge to use
	 * @return true, if at least one object has passed the judgement, false otherwise
	 */
	private boolean judgeMatches(Judge<?> judge) {
		List<MatchingObject> objects = currentMatches.getContents();
		List<Matchable> matchables = objects.stream().map(MatchingObject::getObject).toList();
		List<? extends Judgement> judgements = judge.batchJudge(matchables, query);
		assert judgements.size() == objects.size();
		boolean atLeastOnePass = false;
		for (int i = 0; i < objects.size(); i++) {
			Judgement judgement = judgements.get(i);
			objects.get(i).addJudgement(judgement);
			atLeastOnePass = atLeastOnePass || judgement.isPassed();
		}
		return atLeastOnePass;
	}

	/**
	 * Applies a sorting step to the current matches, removing all which have not passed the last applied judgement.
	 * Requires that at least one judgement has been applied and no result transformation has happened since then.
	 *
	 * @param name the name to apply to the sort directive
	 */
	private void removeMatchesWhichFailedTheLastFiltering(String name) {
		currentMatches.applySortingStep(
				new BinarySortDirective<>(name,
						matchingObject -> matchingObject.getAppliedJudgements().getLast().isPassed(),
						true)
		);
	}

	/**
	 * Applies a sorting step to the current matches, sorting along the score of the last applied judgement. Requires
	 * that the last applied judgement creates {@link ScoredJudgement}-instances and no result transformation has
	 * happened since then.
	 *
	 * @param name            the name to apply to the sort directive
	 * @param retainThreshold the retain threshold to use
	 */
	private void sortMatchesByLatestScoredJudgement(String name, Double retainThreshold) {
		currentMatches.applySortingStep(
				new ScoreSortDirective<>(name,
						matchingObject -> ((ScoredJudgement) matchingObject.getAppliedJudgements()
						                                                   .getLast()).getScore(),
						retainThreshold)
		);
	}

	/**
	 * Returns the current intermediate matches.
	 */
	public List<MatchingObject> getCurrentMatches() {
		return currentMatches.getContents();
	}

	/**
	 * Returns the current matches as {@link SubSortingTree}. Note that modifications to this tree will
	 * affect the matching, in possibly unpredicatable ways.
	 */
	public SubSortingTree<MatchingObject> getCurrentMatchesTree() {
		return currentMatches;
	}

}
