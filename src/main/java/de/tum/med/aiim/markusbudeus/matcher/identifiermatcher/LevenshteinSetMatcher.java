package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This matcher uses Levenshtein Distance to compare sets of strings. Using it is very expensive :(
 */
public class LevenshteinSetMatcher extends ScoreBasedMatcher<Set<String>> {
	private final LevenshteinDistance distance = new LevenshteinDistance(2);

	private final List<String> terms1 = new ArrayList<>();
	private final List<String> terms2 = new ArrayList<>();

	public LevenshteinSetMatcher(IdentifierProvider<Set<String>> provider) {
		super(provider);
	}

	@Override
	public double getScore(Set<String> searchTerm, Set<String> target) {
		terms1.clear();
		terms1.addAll(searchTerm);
		terms2.clear();
		terms2.addAll(target);

		int[][] distanceMatrix = new int[terms1.size()][terms2.size()];

		for (int i = 0; i < distanceMatrix.length; i++) {
			for (int j = 0; j < distanceMatrix[i].length; j++) {
				distanceMatrix[i][j] = distance.apply(terms1.get(i), terms2.get(j));
			}
		}

		int[] bestMatches = new int[terms1.size()];
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

		return score / terms1.size();
	}

}
