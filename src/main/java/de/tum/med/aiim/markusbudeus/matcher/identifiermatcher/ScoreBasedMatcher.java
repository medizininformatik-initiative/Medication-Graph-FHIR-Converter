package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class ScoreBasedMatcher<S> extends IdentifierMatcher<S> {

	public ScoreBasedMatcher(IdentifierProvider<S> provider) {
		super(provider);
	}

	@Override
	public Match<S> findMatchWithoutTransformation(S searchTerm) {
		Collection<MappedIdentifier<S>> allIdentifiers = identifiers.values();
		List<ScoreMultiMatch.MatchWithScore<S>> scores = new ArrayList<>();

		allIdentifiers.forEach(identifier -> {
			double score = getScore(searchTerm, identifier.identifier.getIdentifier());
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
