package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.EditDistance;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringSetUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.InputUsageTraceable;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;
import java.util.stream.Collectors;

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
public class LevenshteinSetMatcher extends SimpleMatcher<Set<String>, LevenshteinSetMatcher.Match> {

	// TODO Rewrite Javadoc
	// TODO Rewrite tests

	private final LevenshteinDistance distance = new LevenshteinDistance(2);

	/**
	 * Calculates a score based on the similarity between the two sets of strings. It compares each pair of entries
	 * which can be taken from these sets. If the score is greater than 0 (i.e. any match is found), a {@link Match} is
	 * returned, otherwise this function returns null.
	 */
	@Override
	public Match match(Set<String> searchTerm, MappedIdentifier<Set<String>> identifier) {
		Set<String> target = identifier.identifier;
		List<EditDistance> resultDistances = new ArrayList<>();
		for (String searchTermToken : searchTerm) {
			int bestScore = Integer.MAX_VALUE;
			String bestMatch = null;
			for (String targetToken : target) {
				int editDistance = distance.apply(searchTermToken, targetToken);
				if (editDistance != -1 && editDistance < bestScore) {
					bestScore = editDistance;
					bestMatch = targetToken;
				}
			}
			if (bestMatch != null) {
				resultDistances.add(new EditDistance(searchTermToken, bestMatch, bestScore));
			}
		}

		double score = 0;
		for (EditDistance d : resultDistances) {
			score += 1.0 / (d.getEditDistance() + 1);
		}

		if (score == 0) return null;

		score = score / searchTerm.size();
		return new Match(identifier, score, resultDistances);

	}

	@Override
	protected boolean supportsParallelism() {
		return true;
	}

	public static class Match extends de.medizininformatikinitiative.medgraph.searchengine.matcher.model.ScoreBasedMatch<Set<String>>
			implements InputUsageTraceable<StringSetUsageStatement> {

		private final StringSetUsageStatement usageStatement;
		private final List<EditDistance> editDistances;

		private Match(MappedIdentifier<Set<String>> match, double score, List<EditDistance> editDistances) {
			super(match, score);
			this.usageStatement = new StringSetUsageStatement(
					match.identifier,
					editDistances.stream().map(EditDistance::getValue1).collect(Collectors.toSet()));
			this.editDistances = editDistances;
		}

		public List<EditDistance> getEditDistances() {
			return editDistances;
		}

		@Override
		public StringSetUsageStatement getUsageStatement() {
			return usageStatement;
		}
	}

}
