package de.tum.med.aiim.markusbudeus.matcher2.matchers;

import de.tum.med.aiim.markusbudeus.matcher2.matchers.model.Match;
import de.tum.med.aiim.markusbudeus.matcher2.matchers.model.ScoreMultiMatch;
import de.tum.med.aiim.markusbudeus.matcher2.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher2.provider.MappedIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ScoreBasedMatcher<S> extends SimpleMatcher<S> {

	@Override
	protected Match<S> findMatch(S searchTerm, IdentifierProvider<S> identifierProvider) {
		Collection<MappedIdentifier<S>> allIdentifiers = identifierProvider.getIdentifiers();
		List<ScoreMultiMatch.MatchWithScore<S>> scores = new ArrayList<>();

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
	public abstract double getScore(S searchTerm, S target);
}
