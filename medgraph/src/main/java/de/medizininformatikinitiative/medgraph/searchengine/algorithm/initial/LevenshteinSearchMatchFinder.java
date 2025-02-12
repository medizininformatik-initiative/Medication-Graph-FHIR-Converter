package de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceSetMatcher;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.LevenshteinDistanceService;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.DetailedMatch;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TrackableIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifierStream;
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
public class LevenshteinSearchMatchFinder implements InitialMatchFinder<Product> {

	private final Transformer<List<String>, Set<String>> TOKEN_TRANSFORMER =
			new CollectionToLowerCase<List<String>>()
					.and(new RemoveBlankStrings())
					.and(new ListToSet());
	private final Transformer<String, Set<String>> IDENTIFIER_TRANSFORMER =
			new RemoveDosageInformation()
					.and(new WhitespaceTokenizer())
					.and(new TrimSpecialSuffixSymbols())
					.and(TOKEN_TRANSFORMER);

	private final EditDistanceSetMatcher levenshteinSetMatcher = new EditDistanceSetMatcher(
			new LevenshteinDistanceService(2));

	private final MappedIdentifierStream<String, Product> productsProvider;

	/**
	 * Creates a new {@link LevenshteinSearchMatchFinder}.
	 *
	 * @param productsProvider the provider of product names and corresponding products in which to search
	 */
	public LevenshteinSearchMatchFinder(BaseProvider<String, Product> productsProvider) {
		this.productsProvider = productsProvider.parallel();
	}

	@Override
	public Stream<OriginalMatch<Product>> findInitialMatches(SearchQuery query) {
		if (query.getProductNameKeywords().isEmpty())
			return Stream.empty();

		TrackableIdentifier<List<String>> productKeywords = new OriginalIdentifier<>(query.getProductNameKeywords(),
				OriginalIdentifier.Source.SEARCH_QUERY);

		return levenshteinSetMatcher.match(TOKEN_TRANSFORMER.apply(productKeywords),
				productsProvider.withTransformation(IDENTIFIER_TRANSFORMER))
		                     .sorted(Comparator.comparing(DetailedMatch::getMatchInfo))
		                     .map(match -> new OriginalMatch<>(
				                     match.getMatchedIdentifier().target,
				                     match.getMatchInfo().getScore(),
				                     new MatchOrigin<>(match, levenshteinSetMatcher)
		                     ));
	}
	@Override
	public void close() {
		// Leere Implementierung, da keine Ressourcenfreigabe
	}

}
