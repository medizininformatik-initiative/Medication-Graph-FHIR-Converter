package de.medizininformatikinitiative.medgraph.matcher.matcher;

import de.medizininformatikinitiative.medgraph.matcher.matcher.model.ScoreMultiMatch;
import de.medizininformatikinitiative.medgraph.matcher.provider.IdentifierProvider;
import de.medizininformatikinitiative.medgraph.matcher.provider.MappedIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A superclass for matchers which assign a score to each identifier which the search term is matched against.
 * @param <S> the type of search term used
 * @param <T> the type of identifiers used
 */
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
