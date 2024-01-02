package de.tum.med.aiim.markusbudeus.matcher.matcher;

import de.tum.med.aiim.markusbudeus.matcher.matcher.model.ScoreMultiMatch;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ScoreBasedMatcher<S, T> extends Matcher<S, T, ScoreMultiMatch<T>> {

	@Override
	protected ScoreMultiMatch<T> findMatch(S searchTerm, IdentifierProvider<T> identifierProvider) {
		Collection<MappedIdentifier<T>> allIdentifiers = identifierProvider.getIdentifiers();
		List<ScoreMultiMatch.MatchWithScore<T>> scores = new ArrayList<>();

		allIdentifiers.forEach(identifier -> {
			double score = getScore(searchTerm, identifier.identifier);
			if (score > 0) scores.add(new ScoreMultiMatch.MatchWithScore<>(identifier, score));
		});
		return new ScoreMultiMatch<>(scores);
	}

	/**
	 * Calculates the matching score between the search term and the target. The score should be between 0 and 1,
	 * inclusive. If a score of zero is given, the match will be exluded from the results.
	 */
	public abstract double getScore(S searchTerm, T target);
}
