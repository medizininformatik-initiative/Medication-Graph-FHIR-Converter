package de.medizininformatikinitiative.medgraph.matcher.algorithm;

import de.medizininformatikinitiative.medgraph.matcher.houselisttransformer.DosageFromNameIdentifier;
import de.medizininformatikinitiative.medgraph.matcher.resulttransformer.FilterProductsWithoutSuccessor;
import de.medizininformatikinitiative.medgraph.matcher.matcher.LevenshteinSetMatcher;
import de.medizininformatikinitiative.medgraph.matcher.matcher.MatcherConfiguration;
import de.medizininformatikinitiative.medgraph.matcher.matcher.SubstringPresenceMatcher;
import de.medizininformatikinitiative.medgraph.matcher.matcher.UnionSizeMatcher;
import de.medizininformatikinitiative.medgraph.matcher.model.HouselistEntry;
import de.medizininformatikinitiative.medgraph.matcher.model.MatchingTarget;
import de.medizininformatikinitiative.medgraph.matcher.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.matcher.provider.IdentifierProvider;
import de.medizininformatikinitiative.medgraph.matcher.provider.MappedIdentifier;
import de.medizininformatikinitiative.medgraph.matcher.provider.TransformedProvider;
import de.medizininformatikinitiative.medgraph.matcher.stringtransformer.*;
import de.medizininformatikinitiative.medgraph.matcher.OngoingMatching;
import de.medizininformatikinitiative.medgraph.matcher.data.SubSortingTree;
import de.medizininformatikinitiative.medgraph.matcher.judge.DosageMatchJudge;
import de.medizininformatikinitiative.medgraph.matcher.resulttransformer.ProductOnlyFilter;
import de.medizininformatikinitiative.medgraph.matcher.resulttransformer.SubstanceToProductResolver;
import org.neo4j.driver.Session;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The matching algorithm used in my Master's thesis.
 *
 * @author Markus Budeus
 */
public class MastersThesisAlgorithm implements MatchingAlgorithm {

	private static final Transformer<String, String> STRING_TRANSFORMER =
			new ToLowerCase()
					.and(new RemoveDosageInformation());
	private static final Transformer<String, Set<String>> TOKEN_TRANSFORMER =
			STRING_TRANSFORMER
					.and(new WhitespaceTokenizer())
					.and(new TrimSpecialSuffixSymbols())
					.and(new RemoveBlankStrings())
					.and(new ListToSet());

	private final IdentifierProvider<String> baseProvider;
	private final UnionSizeMatcher unionSizeMatcher;
	private final LevenshteinSetMatcher levenshteinSetMatcher;

	private final DosageFromNameIdentifier dosageFromNameIdentifier;
	private final SubstanceToProductResolver substanceToProductResolver;
	private final ProductOnlyFilter productOnlyFilter;
	private final DosageMatchJudge dosageMatchJudge;
	private final SubstringPresenceMatcher substringPresenceMatcher;

	private final FilterProductsWithoutSuccessor filterProductsWithoutSuccessor;

	public MastersThesisAlgorithm(Session session) {
		baseProvider = BaseProvider.ofDatabaseSynonymes(session);
		unionSizeMatcher = new UnionSizeMatcher();
		levenshteinSetMatcher = new LevenshteinSetMatcher();
		substringPresenceMatcher = new SubstringPresenceMatcher();

		dosageFromNameIdentifier = new DosageFromNameIdentifier();
		substanceToProductResolver = new SubstanceToProductResolver(session);
		productOnlyFilter = new ProductOnlyFilter();
		dosageMatchJudge = new DosageMatchJudge(session);
		filterProductsWithoutSuccessor = new FilterProductsWithoutSuccessor(session);
	}

	@Override
	public SubSortingTree<MatchingTarget> match(HouselistEntry entry) {
		List<MatchingTarget> initialList = findInitialMatchingTargets(entry);

		if (initialList.isEmpty()) new SubSortingTree<>(initialList);

		OngoingMatching matching = new OngoingMatching(entry, initialList);

		if (matching.getCurrentMatches().size() == 1) {
			List<MatchingTarget> resultList = matching.getCurrentMatches();
			if (resultList.get(0).getType() == MatchingTarget.Type.PRODUCT) {
				return matching.getCurrentMatchesTree();
			}
		}

		// Only use product matches, unless this leaves us without a result. In that case, transform substances
		// to products.
		if (!matching.transformResults(productOnlyFilter, true)) {
			matching.transformResults(substanceToProductResolver);
		}

		dosageFromNameIdentifier.transform(matching.getHouselistEntry());

		matching.applySortingStep("Dosage Match Score", dosageMatchJudge, 0.1, true);

		sortBySubstringsFound(matching);

		matching.applySortingStep("Prefer products without successor", filterProductsWithoutSuccessor);

		return matching.getCurrentMatchesTree();
	}

	private List<MatchingTarget> findInitialMatchingTargets(HouselistEntry entry) {
		Set<? extends MappedIdentifier<?>> result;
		MatcherConfiguration<Set<String>, Set<String>> setConfig = MatcherConfiguration.usingTransformations(
				TOKEN_TRANSFORMER,
				baseProvider
		);
		result = unionSizeMatcher.findMatch(entry, setConfig).getBestMatches();
		if (result.isEmpty()) {
			result = levenshteinSetMatcher.findMatch(entry, setConfig).getBestMatches();
		}
		if (result.isEmpty()) {
			return new ArrayList<>();
		}
		return result.stream().map(i -> i.target).distinct().collect(Collectors.toList());
	}

	private void sortBySubstringsFound(OngoingMatching matching) {
		Transformer<String, String> stringTransformer2 = new ToLowerCase();
		Transformer<String, Set<String>> setTransformer2 = stringTransformer2
				.and(new WhitespaceTokenizer())
				.and(new TrimSpecialSuffixSymbols())
				.and(new RemoveBlankStrings())
				.and(new ListToSet());

		MatcherConfiguration<Set<String>, String> configuration = new MatcherConfiguration<>(
				e -> setTransformer2.transform(e.searchTerm),
				new TransformedProvider<>(BaseProvider.ofMatchingTargetNames(matching.getCurrentMatches()),
						stringTransformer2)
		);
		matching.applySortingStep("Substrings found",
				substringPresenceMatcher.findMatch(matching.getHouselistEntry(), configuration),
				null);
	}

}
