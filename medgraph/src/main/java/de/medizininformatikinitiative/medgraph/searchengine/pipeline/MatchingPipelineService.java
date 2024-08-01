package de.medizininformatikinitiative.medgraph.searchengine.pipeline;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.*;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Judgement;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Transformation;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.Filter;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudgeConfiguration;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer.IMatchTransformer;
import de.medizininformatikinitiative.medgraph.searchengine.tools.Util;

import java.util.*;

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
	public <S extends T, T extends Matchable> List<MatchingObject<S>> applyFilter(List<? extends MatchingObject<S>> objects,
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
			return new ArrayList<>(objects);
		}

		List<MatchingObject<S>> survivors = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			if (passes.get(i)) survivors.add(objects.get(i));
		}
		return survivors;
	}

	/**
	 * Transforms the given objects using the given transformer. If the transformer produces duplicates (including
	 * indirectly, if two different inputs result in the same output), these duplicates are merged.
	 *
	 * @param objects              the objects to transform
	 * @param transformer          the transformer to use
	 * @param scoreMergingStrategy the strategy to use for assigning scores to merges if the transformation produces
	 *                             duplicates
	 * @param <S>                  the type of input {@link Matchable}s
	 * @param <T>                  the type of {@link Matchable}s produced by the transformer
	 * @return a list {@link TransformedObject} instances, possibly intertwined with {@link Merge}-objects
	 */
	public <S extends Matchable, T extends Matchable> List<MatchingObject<T>> transformMatches(
			List<? extends MatchingObject<S>> objects,
			IMatchTransformer<S, T> transformer,
			ScoreMergingStrategy scoreMergingStrategy) {
		return mergeDuplicates(transformMatches(objects, transformer), scoreMergingStrategy);
	}

	/**
	 * Transforms the given objects using the given transformer. Note that if the transformer produces duplicates
	 * (including indirectly, if two different inputs result in the same output), this means the output contains
	 * multiple {@link MatchingObject}-instances referencing the same {@link Matchable}
	 *
	 * @param objects     the objects to transform
	 * @param transformer the transformer to use
	 * @param <S>         the type of input {@link Matchable}s
	 * @param <T>         the type of {@link Matchable}s produced by the transformer
	 * @return a list {@link TransformedObject} instances
	 */
	<S extends Matchable, T extends Matchable> List<TransformedObject<S, T>> transformMatches(List<? extends MatchingObject<S>> objects,
	                                                                                    IMatchTransformer<S, T> transformer) {
		// Step 1: Run the transformer
		List<S> matchables = Util.unpack(objects);
		List<Transformation<T>> transformations = transformer.batchTransform(matchables, query);

		// Verify we got the expected amount of transformations
		int size = matchables.size();
		if (size != transformations.size()) {
			throw new IllegalStateException(
					"The transformer was given " + matchables.size() + " objects to transform, " +
							"but returned " + transformations.size() + " transformations!");
		}

		List<TransformedObject<S, T>> outList = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			Transformation<T> transformation = transformations.get(i);
			MatchingObject<S> source = objects.get(i);
			for (T matchable : transformation.result()) {
				outList.add(new TransformedObject<>(matchable, source, transformation));
			}
		}

		return outList;
	}

	/**
	 * Merges all {@link MatchingObject}-instances in the given list which point to the same {@link Matchable}. Merging
	 * means that a {@link Merge}-instance is created which references the {@link MatchingObject}s that reference the
	 * same object. If an object is only referenced by a single {@link MatchingObject}, it is not merged and directly
	 * inserted into the output list. If there are no duplicates in the input list, the output list will be the same as
	 * the input list.
	 * <p>
	 * This function preserves the list ordering. In case of duplicates, the corresponding {@link Merge}-object is
	 * inserted at the first occurrence of one of its source objects.
	 *
	 * @param objects              the objects to merge
	 * @param scoreMergingStrategy the strategy to use for merging the scores of carriers which reference the same
	 *                             object
	 * @param <S>                  the type of {@link Matchable} held by the objects
	 * @return the input list, with {@link MatchingObject}s which reference the same {@link Matchable} replaced by a
	 * corresponding {@link Merge}.
	 */
	<S extends Matchable> List<MatchingObject<S>> mergeDuplicates(List<? extends MatchingObject<S>> objects,
	                                                              ScoreMergingStrategy scoreMergingStrategy) {
		// Group carrier objects by Matchable
		// Use LinkedHashMap to ensure order to objects in transformation output is preserved.
		Map<Matchable, LinkedList<MatchingObject<S>>> objectsByMatchable = new LinkedHashMap<>();

		for (MatchingObject<S> sourceObject : objects) {
			objectsByMatchable.computeIfAbsent(sourceObject.getObject(), m -> new LinkedList<>()).add(sourceObject);
		}

		// Next, merge elements of all lists with more than one element.
		List<MatchingObject<S>> outList = new ArrayList<>(objectsByMatchable.size());
		for (LinkedList<MatchingObject<S>> group : objectsByMatchable.values()) {
			if (group.isEmpty()) {
				throw new IllegalStateException("Found an empty objects list, altough they are only created once " +
						"an object has been found. This should not be possible. Please investigate.");
			} else if (group.size() == 1) {
				outList.add(group.getFirst());
			} else {
				outList.add(new Merge<>(outList, scoreMergingStrategy));
			}
		}

		return outList;
	}

}
