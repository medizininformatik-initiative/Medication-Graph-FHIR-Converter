package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.matcher.data.ScoreSortDirective;
import de.tum.med.aiim.markusbudeus.matcher.data.SortDirective;
import de.tum.med.aiim.markusbudeus.matcher.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher.houselisttransformer.HouselistTransformer;
import de.tum.med.aiim.markusbudeus.matcher.matchers.IMatcher;
import de.tum.med.aiim.markusbudeus.matcher.matchers.MatcherConfiguration;
import de.tum.med.aiim.markusbudeus.matcher.matchers.model.ScoreMultiMatch;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.provider.BaseProvider;
import de.tum.med.aiim.markusbudeus.matcher.resultranker.MatchJudge;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.ResultTransformer;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.Transformer;

import java.util.*;

public class OngoingMatching {

	private final HouselistEntry entry;
	private final SubSortingTree<MatchingTarget> currentMatches;

	public OngoingMatching(HouselistEntry entry, List<MatchingTarget> initialMatches) {
		this.entry = entry;
		this.currentMatches = new SubSortingTree<>(initialMatches);
	}

	public <S> void applySimpleSortingStep(String name,
									   Transformer<String, S> transformer,
									   IMatcher<S, S, ? extends ScoreMultiMatch<S>> matcher,
	                                   Double retainThreshold) {
		BaseProvider<String> baseProvider = BaseProvider.ofMatchingTargetNames(currentMatches.getContents());
		MatcherConfiguration<S,S> configuration = MatcherConfiguration.usingTransformations(transformer, baseProvider);
		applySortingStep(name, matcher.findMatch(entry, configuration), retainThreshold);
	}

	public void applySortingStep(String name, MatchJudge judge, Double retainThreshold) {
		List<MatchingTarget> targets = currentMatches.getContents();
		List<Double> scores = judge.batchJudge(targets, entry);
		Map<MatchingTarget, Double> assignedScores = new HashMap<>();
		for (int i = 0; i < targets.size(); i++) {
			assignedScores.put(targets.get(i), scores.get(i));
		}
		applySortingStep(new ScoreSortDirective<>(name, assignedScores::get, retainThreshold));
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
	 * @param resultTransformer the result transformer to apply
	 * @param onlyIfNotEmpty if true, the transformation will not be applied if it would eliminate all {@link MatchingTarget}s
	 * @return true if the transformation was applied, false if onlyIfNotEmpty was true and caused the transformation
	 * to not be applied
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
