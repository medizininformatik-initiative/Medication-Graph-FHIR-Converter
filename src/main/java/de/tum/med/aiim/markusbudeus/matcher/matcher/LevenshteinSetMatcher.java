package de.tum.med.aiim.markusbudeus.matcher.matcher;

import de.tum.med.aiim.markusbudeus.matcher.matcher.model.ScoreMultiMatch;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This matcher uses Levenshtein Distance to compare sets of strings. Using it is very expensive :(
 */
public class LevenshteinSetMatcher extends SimpleMatcher<Set<String>, ScoreMultiMatch<Set<String>>> {
	private final LevenshteinDistance distance = new LevenshteinDistance(2);

	@Override
	protected ScoreMultiMatch<Set<String>> findMatch(Set<String> searchTerm, IdentifierProvider<Set<String>> identifierProvider) {
		Collection<MappedIdentifier<Set<String>>> allIdentifiers = identifierProvider.getIdentifiers();
		List<ScoreMultiMatch.MatchWithScore<Set<String>>> scores = new ArrayList<>();
		final List<String> terms1 = new ArrayList<>();
		final List<String> terms2 = new ArrayList<>();
		allIdentifiers.forEach(identifier -> {
			terms1.clear();
			terms1.addAll(searchTerm);
			terms2.clear();
			terms2.addAll(identifier.identifier);
			double score = getScore(terms1, terms2);
			if (score > 0) scores.add(new ScoreMultiMatch.MatchWithScore<>(identifier, score));
		});
		return new ScoreMultiMatch<>(scores);
	}

	public double getScore(List<String> searchTerm, List<String> target) {
		int[][] distanceMatrix = new int[searchTerm.size()][target.size()];

		for (int i = 0; i < distanceMatrix.length; i++) {
			for (int j = 0; j < distanceMatrix[i].length; j++) {
				distanceMatrix[i][j] = distance.apply(searchTerm.get(i), target.get(j));
			}
		}

		int[] bestMatches = new int[searchTerm.size()];
		for (int i = 0; i < distanceMatrix.length; i++) {
			int best = Integer.MAX_VALUE;
			for (int j = 0; j < distanceMatrix[i].length; j++) {
				int current = distanceMatrix[i][j];
				if (current >= 0 && best > current) {
					best = current;
				}
			}
			bestMatches[i] = best;
		}

		double score = 0;
		for (int i : bestMatches) {
			if (i < Integer.MAX_VALUE)
				score += 1.0 / (i + 1);
		}

		return score / searchTerm.size();
	}

}
