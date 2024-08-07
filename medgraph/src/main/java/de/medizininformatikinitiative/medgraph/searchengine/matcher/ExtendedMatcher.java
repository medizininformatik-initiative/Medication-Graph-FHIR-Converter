package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.DetailedMatch;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.MatchInfo;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.IdentifierProvider;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * Extension of {@link Matcher} which produces {@link DetailedMatch}-instances.
 *
 * @param <M> the type of {@link MatchInfo} added to the produced result
 * @author Markus Budeus
 */
public abstract class ExtendedMatcher<S, T, M extends MatchInfo> extends Matcher<S, T> {

	@Override
	public <I extends Identifier<S>, O extends Identifier<T>> Stream<? extends DetailedMatch<I, O, M>> match(
			I searchTerm,
			IdentifierProvider<O> provider) {
		return processAsStream(searchTerm, provider, this::match);
	}

	@Override
	protected <I extends Identifier<S>, O extends Identifier<T>> DetailedMatch<I, O, M> match(I searchTerm,
	                                                                                                   O target) {
		M matchInfo = match(searchTerm.getIdentifier(), target.getIdentifier());
		if (matchInfo == null) return null;
		return new DetailedMatch<>(searchTerm, target, matchInfo);
	}

	/**
	 * Attempts to match the given search term to the given target. If the match succeeds, a {@link MatchInfo}-object
	 * shall be returned with additional information about the match, where applicable. If the match is unsuccessful,
	 * this function returns null.
	 *
	 * @param searchTerm the search term used for matching
	 * @param target the term to match against
	 * @return a {@link MatchInfo} in case of a successful match, otherwise null
	 */
	@Nullable
	protected abstract M match(S searchTerm, T target);

}
