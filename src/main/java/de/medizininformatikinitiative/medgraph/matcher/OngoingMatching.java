package de.medizininformatikinitiative.medgraph.matcher;

import de.medizininformatikinitiative.medgraph.matcher.data.BinarySortDirective;
import de.medizininformatikinitiative.medgraph.matcher.data.ScoreSortDirective;
import de.medizininformatikinitiative.medgraph.matcher.data.SortDirective;
import de.medizininformatikinitiative.medgraph.matcher.data.SubSortingTree;
import de.medizininformatikinitiative.medgraph.matcher.houselisttransformer.HouselistTransformer;
import de.medizininformatikinitiative.medgraph.matcher.matcher.IMatcher;
import de.medizininformatikinitiative.medgraph.matcher.matcher.MatcherConfiguration;
import de.medizininformatikinitiative.medgraph.matcher.matcher.model.ScoreMultiMatch;
import de.medizininformatikinitiative.medgraph.matcher.model.HouselistEntry;
import de.medizininformatikinitiative.medgraph.matcher.model.MatchingTarget;
import de.medizininformatikinitiative.medgraph.matcher.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.matcher.stringtransformer.Transformer;
import de.medizininformatikinitiative.medgraph.matcher.judge.MatchJudge;
import de.medizininformatikinitiative.medgraph.matcher.resulttransformer.Filter;
import de.medizininformatikinitiative.medgraph.matcher.resulttransformer.ResultTransformer;

import java.util.*;

/**
 * Instance which holds intermediate matches of a matching algorithm.
 *
 * @author Markus Budeus
 */
public class OngoingMatching {

	private final HouselistEntry entry;
	private final SubSortingTree<MatchingTarget> currentMatches;

	public OngoingMatching(HouselistEntry entry, List<MatchingTarget> initialMatches) {
		this.entry = entry;
		this.currentMatches = new SubSortingTree<>(initialMatches);
//		this.currentMatches.clearDuplicates();
	}

	public <S> void applySimpleSortingStep(String name,
	                                       Transformer<String, S> transformer,
	                                       IMatcher<S, S, ? extends ScoreMultiMatch<S>> matcher,
	                                       Double retainThreshold) {
		BaseProvider<String> baseProvider = BaseProvider.ofMatchingTargetNames(currentMatches.getContents());
		MatcherConfiguration<S, S> configuration = MatcherConfiguration.usingTransformations(transformer, baseProvider);
		applySortingStep(name, matcher.findMatch(entry, configuration), retainThreshold);
	}

	public boolean applySortingStep(String name, MatchJudge judge, Double retainThreshold, boolean unlessEmpty) {
		List<MatchingTarget> targets = currentMatches.getContents();
		List<Double> scores = judge.batchJudge(targets, entry);
		Map<MatchingTarget, Double> assignedScores = new HashMap<>();
		boolean empty = true;
		for (int i = 0; i < targets.size(); i++) {
			double score = scores.get(i);
			assignedScores.put(targets.get(i), score);
			if (retainThreshold != null && score >= retainThreshold) {
				empty = false;
			}
		}
		if (unlessEmpty && empty) {
			return false;
		}
		applySortingStep(new ScoreSortDirective<>(name, assignedScores::get, retainThreshold));
		return true;
	}

	public void applySortingStep(String name, ScoreMultiMatch<?> match, Double retainThreshold) {
		Map<MatchingTarget, Double> scoreMap = new HashMap<>();
		match.matchesWithScore.forEach(m -> scoreMap.put(m.match.target, m.score));
		applySortingStep(new ScoreSortDirective<>(
				name,
				scoreMap::get,
				retainThreshold
		));
	}

	/**
	 * Applies a binary sorting step, preferring matching targets who pass the given filter. If you want to eliminate
	 * results which do not pass the filter, use {@link #transformResults(ResultTransformer)} instead.
	 */
	public void applySortingStep(String name, Filter filter) {
		List<MatchingTarget> targetList = currentMatches.getContents();
		List<Boolean> resultList = filter.batchPassesFilter(targetList, entry);
		SortDirective<MatchingTarget> directive = new BinarySortDirective<>(
				name,
				target -> resultList.get(targetList.indexOf(target)),
				false
		);
	}

	public void applySortingStep(SortDirective<MatchingTarget> sortDirective) {
		currentMatches.applySortingStep(sortDirective);
	}

	public void transformHouselistEntry(HouselistTransformer transformer) {
		transformer.transform(entry);
	}

	public void transformResults(ResultTransformer resultTransformer) {
		transformResults(resultTransformer, false);
	}

	/**
	 * Transforms the current {@link MatchingTarget}s using the given {@link ResultTransformer}.
	 *
	 * @param resultTransformer the result transformer to apply
	 * @param onlyIfNotEmpty    if true, the transformation will not be applied if it would eliminate all
	 *                          {@link MatchingTarget}s
	 * @return true if the transformation was applied, false if onlyIfNotEmpty was true and caused the transformation to
	 * not be applied
	 */
	public boolean transformResults(ResultTransformer resultTransformer, boolean onlyIfNotEmpty) {
		Map<MatchingTarget, List<MatchingTarget>> transformationResults = new HashMap<>();
		List<MatchingTarget> allTargets = new HashSet<>(currentMatches.getContents()).stream().toList();
		List<List<MatchingTarget>> transformedMatchingTargets = resultTransformer.batchTransform(allTargets, entry);
		for (int i = 0; i < allTargets.size(); i++) {
			transformationResults.put(allTargets.get(i), transformedMatchingTargets.get(i));
		}

		if (onlyIfNotEmpty) {
			boolean empty = true;
			for (List<MatchingTarget> targets : transformedMatchingTargets) {
				if (!targets.isEmpty()) {
					empty = false;
					break;
				}
			}
			if (empty)
				return false;
		}

		currentMatches.batchReplace(transformationResults);
		currentMatches.clearDuplicates();
		return true;
	}

	/**
	 * The houselist entry currently being matched.
	 */
	public HouselistEntry getHouselistEntry() {
		return entry;
	}

	public SubSortingTree<MatchingTarget> getCurrentMatchesTree() {
		return currentMatches;
	}

	public List<MatchingTarget> getCurrentMatches() {
		return currentMatches.getContents();
	}

}
