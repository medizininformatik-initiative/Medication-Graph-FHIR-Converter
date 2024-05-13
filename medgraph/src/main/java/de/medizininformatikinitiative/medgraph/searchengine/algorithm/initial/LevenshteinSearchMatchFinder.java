package de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.LevenshteinSetMatcher;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.ScoreBasedMatch;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.*;

import java.util.Set;
import java.util.stream.Stream;

/**
 * This initial match finder relies on matchers working with the Levenshtein Distance to find initial matches. It
 * searches known products and substances based on {@link SearchQuery#getProductName()} and
 * {@link SearchQuery#getSubstanceName()}.
 *
 * @author Markus Budeus
 */
public class LevenshteinSearchMatchFinder implements InitialMatchFinder {

	private final Transformer<String, String> STRING_TRANSFORMER =
			new ToLowerCase()
					.and(new RemoveDosageInformation());
	private final Transformer<String, Set<String>> TOKEN_TRANSFORMER =
			STRING_TRANSFORMER
					.and(new WhitespaceTokenizer())
					.and(new TrimSpecialSuffixSymbols())
					.and(new RemoveBlankStrings())
					.and(new ListToSet());

	private final LevenshteinSetMatcher levenshteinSetMatcher = new LevenshteinSetMatcher();

	private final BaseProvider<String> productsProvider;
	private final BaseProvider<String> substanceProvider;

	/**
	 * Creates a new {@link LevenshteinSearchMatchFinder}.
	 *
	 * @param productsProvider  the provider of product names and corresponding products in which to search
	 * @param substanceProvider the provider of substance names and corresponding substances in which to search
	 */
	public LevenshteinSearchMatchFinder(BaseProvider<String> productsProvider, BaseProvider<String> substanceProvider) {
		this.productsProvider = productsProvider;
		this.substanceProvider = substanceProvider;
	}

	@Override
	public Stream<OriginalMatch> findInitialMatches(SearchQuery query) {
		// TODO This looks repetitive

		Stream<ScoreBasedMatch<Set<String>>> allMatches = Stream.empty();
		if (query.getProductName() != null) {
			allMatches = levenshteinSetMatcher.match(
					TOKEN_TRANSFORMER.apply(query.getProductName()),
					productsProvider.withTransformation(TOKEN_TRANSFORMER));
		}
		if (query.getSubstanceName() != null) {
			allMatches = Stream.concat(allMatches, levenshteinSetMatcher.match(
					TOKEN_TRANSFORMER.apply(query.getSubstanceName()),
					substanceProvider.withTransformation(TOKEN_TRANSFORMER)));
		}

		// TODO This throws away all match info, which might be nice to have in the OriginalMatch instance for later reference
		return allMatches.map(match -> new OriginalMatch((Matchable) match.getMatchedIdentifier().target));
	}

}
