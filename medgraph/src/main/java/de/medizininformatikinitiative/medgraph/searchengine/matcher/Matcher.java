package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.Match;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.IdentifierProvider;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Implementation framework for {@link IMatcher}. Takes care of proper stream processing.
 *
 * @author Markus Budeus
 */
public abstract class Matcher<S, T> implements IMatcher<S, T> {

	@Override
	public <I extends Identifier<S>, O extends Identifier<T>> Stream<? extends Match<I, O>> match(I searchTerm,
	                                                                                              IdentifierProvider<O> provider) {
		return processAsStream(searchTerm, provider, this::match);
	}

	/**
	 * This function essentially implements {@link #match(Identifier, IdentifierProvider)}, but with a more generic
	 * return type, allowing you to easily constrain the return type of the
	 * {@link #match(Identifier, IdentifierProvider)} function via overriding.
	 */
	protected <I extends Identifier<S>, O extends Identifier<T>, R> Stream<R> processAsStream(I searchTerm,
	                                                                                          IdentifierProvider<O> provider,
	                                                                                          BiFunction<I, O, R> mappingFunction) {
		Stream<O> identifierStream = provider.getIdentifiers();
		if (!supportsParallelism() && identifierStream.isParallel()) {
			throw new IllegalArgumentException("This matcher does not support parallel execution! Please provide a " +
					"IdentifierProvider with parallel processing disabled.");
		}

		return identifierStream.map(identifier -> mappingFunction.apply(searchTerm, identifier))
		                       .filter(Objects::nonNull);
	}

	/**
	 * Matches the given search term against the given target term.
	 *
	 * @param searchTerm the search term
	 * @param target         the {@link Identifier} to compare the search term against
	 * @return a {@link Match} instance if the matching is successful, otherwise null
	 */
	@Nullable
	protected abstract <I extends Identifier<S>, O extends Identifier<T>> Match<I, O> match(I searchTerm, O target);

	/**
	 * Reports whether this matcher supports parallel matching of target terms against the search term.
	 *
	 * @return true if this matcher supports parallel matching, false otherwise
	 */
	protected abstract boolean supportsParallelism();

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
