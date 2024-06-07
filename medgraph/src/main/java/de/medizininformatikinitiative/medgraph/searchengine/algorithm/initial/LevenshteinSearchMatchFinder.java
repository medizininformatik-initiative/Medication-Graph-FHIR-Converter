package de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceSetMatcher;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.LevenshteinDistanceService;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.IdentifierStream;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This initial match finder relies on matchers working with the Levenshtein Distance to find initial matches. It
 * searches known products based on {@link SearchQuery#getProductNameKeywords()}.
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

	private final EditDistanceSetMatcher levenshteinSetMatcher = new EditDistanceSetMatcher(new LevenshteinDistanceService(2));

	private final IdentifierStream<String> productsProvider;

	/**
	 * Creates a new {@link LevenshteinSearchMatchFinder}.
	 *
	 * @param productsProvider  the provider of product names and corresponding products in which to search
	 */
	public LevenshteinSearchMatchFinder(BaseProvider<String> productsProvider) {
		this.productsProvider = productsProvider.parallel();
	}

	@Override
	public Stream<OriginalMatch> findInitialMatches(SearchQuery query) {
		Stream<EditDistanceSetMatcher.Match> allMatches = Stream.empty();
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
