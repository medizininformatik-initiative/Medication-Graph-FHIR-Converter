package de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.LevenshteinSetMatcher;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.ScoreBasedMatch;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.IdentifierProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.IdentifierStream;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This initial match finder relies on matchers working with the Levenshtein Distance to find initial matches. It
 * searches known products and substances based on {@link SearchQuery#getProductNameKeywords()} and
 * {@link SearchQuery#getSubstanceNameKeywords()}.
 *
 * @author Markus Budeus
 */
public class LevenshteinSearchMatchFinder implements InitialMatchFinder {

	private final Transformer<List<String>, Set<String>> TOKEN_TRANSFORMER =
			new CollectionToLowerCase<List<String>>()
					.and(new RemoveBlankStrings())
					.and(new ListToSet());
	private final Transformer<String, Set<String>> IDENTIFIER_TRANSFORMER =
			new WhitespaceTokenizer()
					.and(new TrimSpecialSuffixSymbols())
					.and(TOKEN_TRANSFORMER);

	private final LevenshteinSetMatcher levenshteinSetMatcher = new LevenshteinSetMatcher();

	private final IdentifierStream<String> productsProvider;
	private final IdentifierStream<String> substanceProvider;

	/**
	 * Creates a new {@link LevenshteinSearchMatchFinder}.
	 *
	 * @param productsProvider  the provider of product names and corresponding products in which to search
	 * @param substanceProvider the provider of substance names and corresponding substances in which to search
	 */
	public LevenshteinSearchMatchFinder(BaseProvider<String> productsProvider, BaseProvider<String> substanceProvider) {
		this.productsProvider = productsProvider.parallel();
		this.substanceProvider = substanceProvider.parallel();
	}

	@Override
	public Stream<OriginalMatch> findInitialMatches(SearchQuery query) {
		Stream<LevenshteinSetMatcher.Match> allMatches = Stream.empty();
		List<String> productKeywords = query.getProductNameKeywords();
		if (!productKeywords.isEmpty()) {
			allMatches = levenshteinSetMatcher.match(TOKEN_TRANSFORMER.apply(productKeywords),
					productsProvider.withTransformation(IDENTIFIER_TRANSFORMER));
		}

		// TODO This throws away all match info, which might be nice to have in the OriginalMatch instance for later reference
		return allMatches
				.sorted(Comparator.reverseOrder())
				.map(match -> new OriginalMatch((Matchable) match.getMatchedIdentifier().target));
	}

}
