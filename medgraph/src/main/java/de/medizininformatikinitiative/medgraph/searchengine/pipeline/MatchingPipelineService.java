package de.medizininformatikinitiative.medgraph.searchengine.pipeline;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.JudgedObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Judgement;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.Filter;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudgeConfiguration;
import de.medizininformatikinitiative.medgraph.searchengine.tools.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class which provides utility functions to apply matching pipeline components to
 * {@link MatchingObject}-instances.
 *
 * @author Markus Budeus
 */
public class MatchingPipelineService {

	// TODO Test this class!

	private final SearchQuery query;

	public MatchingPipelineService(SearchQuery query) {
		this.query = query;
	}

	/**
	 * Sorts the current matches using the given {@link ScoreJudge}. Non-passing matches are eliminated from the
	 * matching process.
	 *
	 * @param objects       the objects to apply the judge to
	 * @param judge         the judge to use
	 * @param configuration some options to use when applying the judge
	 */
	public <S extends T, T extends Matchable> List<MatchingObject<S>> applyScoreJudge(List<MatchingObject<S>> objects,
	                                                                                  ScoreJudge<T> judge,
	                                                                                  ScoreJudgeConfiguration configuration
	) {
		List<ScoredJudgement> judgements = judge.batchJudge(Util.unpack(objects), query);

		int n = objects.size();
		if (judgements.size() != n) {
			throw new IllegalStateException(
					"Received an invalid number of judgements! Passed " + n + " objects, but got "
							+ judgements.size() + " judgements!");
		}

		List<MatchingObject<S>> outList = new ArrayList<>(n);
		List<MatchingObject<S>> eliminatedList = new ArrayList<>(n);
		double passingScore = configuration.passingScore() != null ? configuration.passingScore() : Double.MIN_VALUE;
		for (int i = 0; i < n; i++) {
			Judgement judgement = judgements.get(i);
			JudgedObject<S> outObject = new JudgedObject<>(objects.get(i), judgement, configuration);

			if (judgement.getScore() >= passingScore) {
				outList.add(outObject);
			} else {
				eliminatedList.add(outObject);
			}
		}

		if (outList.isEmpty() && !configuration.mayEliminateAll()) {
			// If the outList is empty, that means no object passed the judgement. Which in turn means
			// all checked objects are in the eliminatedList. If we may not eliminate all objects, we simply
			// return the eliminated list, which contains all judged objects.
			return eliminatedList;
		}
		return outList;
	}

	/**
	 * Filters the given matches using the given filter.
	 *
	 * @param objects        the objects to filter
	 * @param filter         the filter to use
	 * @param ensureSurvival if this is true, it prevents the elimination of all matches in case no match passes the
	 *                       filter
	 * @return the filtered list of {@link MatchingObject}s
	 */
	public <S extends T, T extends Matchable> List<MatchingObject<S>> applyFilter(List<MatchingObject<S>> objects,
	                                                                              Filter<S> filter,
	                                                                              boolean ensureSurvival) {
		List<Boolean> passes = filter.batchPassesFilter(Util.unpack(objects), query);

		int n = objects.size();
		if (passes.size() != n) {
			throw new IllegalStateException(
					"Received an invalid number of filter results! Passed " + n + " objects, but got "
							+ passes.size() + " results!");
		}

		if (passes.isEmpty() && ensureSurvival) {
			return objects;
		}

		List<MatchingObject<S>> survivors = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			if (passes.get(i)) survivors.add(objects.get(i));
		}
		return survivors;
	}
//
//	public void transformMatches(IMatchTransformer transformer) {
//		// This function is sadly a lot more complicated than applying a judgement.
//		//
//		// First, we feed all current matches into the transformer.
//		// Then, we construct new TransformedObject-instances for every output of each transformation.
//		// Now, intuitively, the final step is to replace each MatchingObject with its transformations using
//		// SubSortingTree.batchReplace.
//		//
//		// However, there is one issue: The MatchTransformer may have generated the same output within the
//		// transformations of different inputs. For example, say we transform substances into products by searching
//		// for products which use the substance as their active ingredient. But, product [A] may be a combination
//		// product, containing substances [B] and [C]. Both [B] and [C] are then resolved to product [A].
//		// Now, [A] exists twice in the resulting matches. Which means, we need to merge them.
//		//
//		// The way we do that is we group all TransformedObject-instances by their Matchable in a hash table.
//		// Then, everytime a Matchable is linked to multiple TransformedObject-instances, we merge them.
//		// Finally, we do the batchReplace. For groups, we replace the first occurrence on the matches (i.e. the
//		// highest-rated) with the Merge object and eliminate the others.
//
//		// Step 1: Run the transformer
//		List<MatchingObject<?>> matchingObjects = getCurrentMatches();
//		List<? extends Matchable> matchables = matchingObjects.stream().map(MatchingObject::getObject).toList();
//		List<Transformation> transformations = transformer.batchTransform(matchables, query);
//
//		// Verify we got the expected amount of transformations
//		int size = matchables.size();
//		if (size != transformations.size()) {
//			throw new IllegalStateException(
//					"The transformer was given " + matchables.size() + " objects to transform, " +
//							"but returned " + transformations.size() + " transformations!");
//		}
//
//		// Step 2: create TransformedObject-instances and group by outcome Matchable
//		// Use LinkedHashMap to ensure order to objects in transformation output is preserved.
//		Map<Matchable, LinkedList<TransformedObject<?, ?>>> postTransformationMap = new LinkedHashMap<>();
//		for (int i = 0; i < size; i++) {
//			MatchingObject<?> sourceObject = matchingObjects.get(i);
//			Transformation transformation = transformations.get(i);
//			for (Matchable output : transformation.result()) {
//				TransformedObject<?, ?> transformedObject = new TransformedObject<>(output, sourceObject, transformation);
//				// Add to the list of TransformedObjects which represent this Matchable, creating the list if required
//				postTransformationMap.computeIfAbsent(output, m -> new LinkedList<>()).add(transformedObject);
//			}
//		}
//
//		// Step 3: Merge all TransformedObjects which reference the same Matchable.
//		// While doing that, prepare the replacement map for the SubSortingTree.
//		Map<MatchingObject<?>, List<MatchingObject<?>>> replacementMap = new HashMap<>();
//		for (MatchingObject<?> sourceObject : matchingObjects) {
//			replacementMap.put(sourceObject, new LinkedList<>());
//		}
//		for (LinkedList<TransformedObject<?, ?>> list : postTransformationMap.values()) {
//			MatchingObject<?> result;
//			assert !list.isEmpty();
//			if (list.size() == 1) {
//				result = list.getFirst();
//			} else {
//				result = new Merge<>(list);
//			}
//			// The remappingKey is the object in the SubSortingTree which is to be replaced with the result.
//			// In case of a single-entry list, this is trivial. We simply replace the source object with the
//			// transformed one.
//			// For a merge, we don't want the Merge to be inserted multiple times, once for each of the merged objects.
//			// Instead we use the position closest to the beginning of the list (i.e. the highest-rated position.)
//			// The object at this position is the source object of the first transformed object in the list, because
//			// the processing happened in this order. So elements further back in the original list are processed later
//			// and therefore end up at later positions in the list of transformed objects.
//			MatchingObject<?> remappingKey = list.getFirst().getSourceObject();
//
//			replacementMap.get(remappingKey).add(result);
//		}
//
//		// Finally, replace MatchingObjects in the SubSortingTree by their TransformedObject or Merge object
//		currentMatches.batchReplace(replacementMap);
//	}
//
//	/**
//	 * Runs the given judge against all current matches and assigns its judgement to the corresponding
//	 * {@link MatchingObject}s.
//	 *
//	 * @param judge the judge to use
//	 * @return true, if at least one object has passed the judgement, false otherwise
//	 */
//	private boolean judgeMatches(Judge<?> judge) {
//		List<MatchingObject<?>> objects = currentMatches.getContents();
//		List<? extends Matchable> matchables = objects.stream().map(MatchingObject::getObject).toList();
//		List<? extends Judgement> judgements = judge.batchJudge(matchables, query);
//		assert judgements.size() == objects.size();
//		boolean atLeastOnePass = false;
//		for (int i = 0; i < objects.size(); i++) {
//			Judgement judgement = judgements.get(i);
//			objects.get(i).addJudgement(judgement);
//			atLeastOnePass = atLeastOnePass || judgement.passed();
//		}
//		return atLeastOnePass;
//	}
//
//	/**
//	 * Applies a sorting step to the current matches, removing all which have not passed the last applied judgement.
//	 * Requires that at least one judgement has been applied and no result transformation has happened since then.
//	 *
//	 * @param name the name to apply to the sort directive
//	 */
//	private void removeMatchesWhichFailedTheLastFiltering(String name) {
//		currentMatches.applySortingStep(
//				new BinarySortDirective<>(name,
//						matchingObject -> matchingObject.getAppliedJudgements().getLast().passed(),
//						true)
//		);
//	}
//
//	/**
//	 * Applies a sorting step to the current matches, sorting along the score of the last applied judgement. Requires
//	 * that the last applied judgement creates {@link ScoredJudgement}-instances and no result transformation has
//	 * happened since then.
//	 *
//	 * @param name            the name to apply to the sort directive
//	 * @param retainThreshold the retain threshold to use
//	 */
//	private void sortMatchesByLatestScoredJudgement(String name, Double retainThreshold) {
//		currentMatches.applySortingStep(
//				new ScoreSortDirective<>(name,
//						matchingObject -> ((ScoredJudgement) matchingObject.getAppliedJudgements()
//						                                                   .getLast()).getScore(),
//						retainThreshold)
//		);
//	}
//
//	/**
//	 * Returns the current intermediate matches.
//	 */
//	public List<MatchingObject<?>> getCurrentMatches() {
//		return currentMatches.getContents();
//	}
//
//	/**
//	 * Returns the current matches as {@link SubSortingTree}. Note that modifications to this tree will
//	 * affect the matching, in possibly unpredicatable ways.
//	 */
//	public SubSortingTree<MatchingObject<?>> getCurrentMatchesTree() {
//		return currentMatches;
//	}
//


}
