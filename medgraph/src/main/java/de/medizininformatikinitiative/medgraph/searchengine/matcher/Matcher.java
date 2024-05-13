package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.Match;
import de.medizininformatikinitiative.medgraph.searchengine.provider.IdentifierProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Implementation framework for {@link IMatcher}. Takes care of proper stream processing.
 *
 * @author Markus Budeus
 */
public abstract class Matcher<S, T, R extends Match<S>> implements IMatcher<S, T, R> {

	@Override
	public Stream<R> match(T searchTerm, IdentifierProvider<S> provider) {
		Stream<MappedIdentifier<S>> identifierStream = provider.getIdentifiers();
		if (!supportsParallelism() && identifierStream.isParallel()) {
			throw new IllegalArgumentException("This matcher does not support parallel execution! Please provide a " +
					"IdentifierProvider with parallel processing disabled.");
		}

		return identifierStream.map(identifier -> match(searchTerm, identifier))
				.filter(Objects::nonNull);
	}

	/**
	 * Matches the given search term against the given identifier.
	 *
	 * @param searchTerm the search term
	 * @param identifier the identifier to compare the search term against
	 * @return a {@link Match} instance if the matching is successful, otherwise null
	 */
	@Nullable
	protected abstract R match(T searchTerm, MappedIdentifier<S> identifier);

	/**
	 * Reports whether this matcher supports parallel matching of target terms against the search term.
	 *
	 * @return true if this matcher supports parallel matching, false otherwise
	 */
	protected abstract boolean supportsParallelism();

}
