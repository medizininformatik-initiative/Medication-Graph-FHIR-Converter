package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Iterator;
import java.util.Set;

/**
 * This matcher uses Levenshtein Distance to compare sets of strings. It works by finding the best-matching token from
 * the target set for each token in the search term set. Then, each word from the search term gets a score based on the
 * Levenshtein Distance to the best-matching token. This score is (1 / (distance + 1)). However, the score is 0 if the
 * distance exceeds 2. Then, the sum of scores from each token from the search term is divided by the amount of tokens
 * in the search term.
 * <p>
 * By design, this results in a score between 0 and 1.
 * <p>
 * Also, using this matcher is probably expensive. (I'm not sure, it performed much better than I had expected during
 * the thesis.) At least it is capable of parallel execution when a parallel stream is used to provide the identifiers
 * (which wasn't the case in the master's thesis version.).
 *
 * @author Markus Budeus
 */
public class LevenshteinSetMatcher extends ScoreBasedMatcher<Set<String>> {
	private final LevenshteinDistance distance = new LevenshteinDistance(2);

	/**
	 * Calculates a score based on the similarity between the two sets of strings. It compares each pair of entries
	 * which can be taken from these sets.
	 */
	public double calculateScore(Set<String> searchTerm, Set<String> target) {
		int[][] distanceMatrix = new int[searchTerm.size()][target.size()];

		Iterator<String> searchTermIterator = searchTerm.iterator();
		for (int i = 0; i < distanceMatrix.length; i++) {
			String searchTermValue = searchTermIterator.next();
			Iterator<String> targetIterator = target.iterator();
			for (int j = 0; j < distanceMatrix[i].length; j++) {
				distanceMatrix[i][j] = distance.apply(searchTermValue, targetIterator.next());
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

	@Override
	protected boolean supportsParallelism() {
		return true;
	}
}
