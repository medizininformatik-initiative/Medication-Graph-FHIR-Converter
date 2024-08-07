package de.medizininformatikinitiative.medgraph.searchengine.pipeline;

import de.medizininformatikinitiative.medgraph.searchengine.model.ScoreJudgedObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.*;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.FilteringStep;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Judgement;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgementStep;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.*;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer.IMatchTransformer;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer.Transformation;
import de.medizininformatikinitiative.medgraph.searchengine.tools.Util;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

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
	 * Sorts the current matches using the given {@link ScoreJudge}. Uses the default score judge configuration.
	 *
	 * @param objects the objects to apply the judge to
	 * @param judge   the judge to use
	 * @see #applyScoreJudge(List, ScoreJudge, ScoreJudgeConfiguration)
	 */
	public <S extends T, T extends Matchable> List<ScoreJudgedObject<S>> applyScoreJudge(
			List<? extends MatchingObject<S>> objects,
			ScoreJudge<T> judge
	) {
		return applyScoreJudge(objects, judge, new ScoreJudgeConfiguration());
	}

	/**
	 * Sorts the current matches using the given {@link ScoreJudge}. Non-passing matches are eliminated from the
	 * matching process.
	 *
	 * @param objects       the objects to apply the judge to
	 * @param judge         the judge to use
	 * @param configuration some options to use when applying the judge
	 */
	public <S extends T, T extends Matchable> List<ScoreJudgedObject<S>> applyScoreJudge(
			List<? extends MatchingObject<S>> objects,
			ScoreJudge<T> judge,
			ScoreJudgeConfiguration configuration
	) {
		List<ScoreJudgementInfo> judgements = judge.batchJudge(Util.unpack(objects), query);

		int n = objects.size();
		if (judgements.size() != n) {
			throw new IllegalStateException(
					"Received an invalid number of judgements! Passed " + n + " objects, but got "
							+ judgements.size() + " judgements!");
		}

		String judgeName = judge.toString();
		String judgeDesc = judge.getDescription();

		return appendJudgementAndFilter(
				objects,
				i -> new ScoredJudgementStep(
						judgeName,
						judgeDesc,
						judgements.get(i),
						configuration
				),
				ScoreJudgedObject::new,
				configuration.mayEliminateAll()
		);
	}

	/**
	 * Filters the given matches using the given filter.
	 *
	 * @param objects         the objects to filter
	 * @param filter          the filter to use
	 * @param mayEliminateall if this is false, it prevents the elimination of all matches in case no match passes the
	 *                        filter
	 * @return the filtered list of {@link MatchingObject}s
	 */
	public <S extends T, T extends Matchable> List<JudgedObject<S>> applyFilter(
			List<? extends MatchingObject<S>> objects,
			Filter<T> filter,
			boolean mayEliminateall) {
		List<FilteringInfo> filterResults = filter.batchJudge(Util.unpack(objects), query);

		int n = objects.size();
		if (filterResults.size() != n) {
			throw new IllegalStateException(
					"Received an invalid number of filter results! Passed " + n + " objects, but got "
							+ filterResults.size() + " results!");
		}

		String filterName = filter.toString();
		String filterDesc = filter.getDescription();

		return appendJudgementAndFilter(
				objects,
				i -> new FilteringStep(
						filterName,
						filterDesc,
						filterResults.get(i)
				),
				JudgedObject::new,
				mayEliminateall
		);
	}

	/**
	 * For each object in the given list, creates a new {@link JudgedObject} and appends it to the chain. Then, filters
	 * the resulting objects based on whether they passed the judgement. If not, they are excluded from the result.
	 *
	 * @param objects             the objects for which to append the judgment information to the chain
	 * @param judgementCreator    a function which generates the corresponding {@link Judgement} based on the index in
	 *                            the list of objects
	 * @param judgedObjectWrapper a function which, given the base object and previously generated {@link Judgement},
	 *                            creates the new {@link JudgedObject} for the chain
	 * @param mayEliminateAll     if this is set to false and if not a single object passed the judgment, this function
	 *                            returns all generated {@link JudgedObject} instead of eliminating them all, which
	 *                            would result in an empty list
	 * @param <S>                 the type of value held by the {@link JudgedObject}s
	 * @param <T>                 the type of {@link Judgement} to work with
	 * @param <O>                 the output {@link MatchingObject} type
	 * @return a list of the "surviving" {@link JudgedObject}s
	 */
	private <S extends Matchable, O extends JudgedObject<S>, T extends Judgement> List<O> appendJudgementAndFilter(
			List<? extends MatchingObject<S>> objects,
			Function<Integer, T> judgementCreator,
			BiFunction<MatchingObject<S>, T, O> judgedObjectWrapper,
			boolean mayEliminateAll
	) {
		int n = objects.size();
		List<O> outList = new ArrayList<>(n);
		List<O> eliminatedList = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			T judgement = judgementCreator.apply(i);
			O outObject = judgedObjectWrapper.apply(objects.get(i), judgement);

			if (judgement.passed()) {
				outList.add(outObject);
			} else {
				eliminatedList.add(outObject);
			}
		}

		if (outList.isEmpty() && !mayEliminateAll) {
			// If the outList is empty, that means no object passed the judgement. Which in turn means
			// all checked objects are in the eliminatedList. If we may not eliminate all objects, we simply
			// return the eliminated list, which contains all judged objects.
			return eliminatedList;
		}
		return outList;
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
		return mergeDuplicates(transformMatchesWithoutMergingDuplicates(objects, transformer), scoreMergingStrategy);
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
	<S extends Matchable, T extends Matchable> List<TransformedObject<S, T>> transformMatchesWithoutMergingDuplicates(
			List<? extends MatchingObject<S>> objects,
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
	public <S extends Matchable> List<MatchingObject<S>> mergeDuplicates(List<? extends MatchingObject<S>> objects,
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
				outList.add(new Merge<>(group, scoreMergingStrategy));
			}
		}

		return outList;
	}

}
