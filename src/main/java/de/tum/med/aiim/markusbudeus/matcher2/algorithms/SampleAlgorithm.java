package de.tum.med.aiim.markusbudeus.matcher2.algorithms;

import de.tum.med.aiim.markusbudeus.matcher2.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher2.OngoingMatching;
import de.tum.med.aiim.markusbudeus.matcher2.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher2.houselisttransformer.DosageFromNameIdentifier;
import de.tum.med.aiim.markusbudeus.matcher2.matchers.LevenshteinMatcher;
import de.tum.med.aiim.markusbudeus.matcher2.matchers.LevenshteinSetMatcher;
import de.tum.med.aiim.markusbudeus.matcher2.matchers.MatcherConfiguration;
import de.tum.med.aiim.markusbudeus.matcher2.matchers.UnionSizeMatcher;
import de.tum.med.aiim.markusbudeus.matcher2.model.MatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher2.provider.BaseProvider;
import de.tum.med.aiim.markusbudeus.matcher2.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher2.provider.MappedIdentifier;
import de.tum.med.aiim.markusbudeus.matcher2.resulttransformer.DosageFilter;
import de.tum.med.aiim.markusbudeus.matcher2.resulttransformer.ProductOnlyFilter;
import de.tum.med.aiim.markusbudeus.matcher2.resulttransformer.SubstanceToProductResolver;
import de.tum.med.aiim.markusbudeus.matcher2.stringtransformer.*;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SampleAlgorithm implements MatchingAlgorithm {

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
	private final LevenshteinMatcher levenshteinMatcher;
	private final LevenshteinSetMatcher levenshteinSetMatcher;

	private final DosageFromNameIdentifier dosageFromNameIdentifier;
	private final DosageFilter dosageFilter;
	private final SubstanceToProductResolver substanceToProductResolver;
	private final ProductOnlyFilter productOnlyFilter;

	public SampleAlgorithm(Session session) {
		baseProvider = BaseProvider.ofDatabaseSynonymes(session);
		unionSizeMatcher = new UnionSizeMatcher();
		levenshteinSetMatcher = new LevenshteinSetMatcher();
		levenshteinMatcher = new LevenshteinMatcher();

		dosageFromNameIdentifier = new DosageFromNameIdentifier();
		dosageFilter = new DosageFilter(session);
		substanceToProductResolver = new SubstanceToProductResolver(session);
		productOnlyFilter = new ProductOnlyFilter();
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

		dosageFromNameIdentifier.transform(matching.getHouselistEntry());

		// Only use product matches, unless this leaves us without a result. In that case, transform substances
		// to products.
		if (!matching.transformResults(productOnlyFilter, true)) {
			matching.transformResults(substanceToProductResolver);

			// Since we now resolved substances into products, we once again prefer products whose name also
			// matches the search term.
			matching.applySimpleSortingStep("Prefer partial name matches",
					TOKEN_TRANSFORMER,
					unionSizeMatcher,
					0.0);
		}
		matching.transformResults(dosageFilter, true);

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
			MatcherConfiguration<String, String> stringConfig = MatcherConfiguration.usingTransformations(
					STRING_TRANSFORMER,
					baseProvider
			);
			result = levenshteinMatcher.findMatch(entry, stringConfig).getBestMatches();
		}
		if (result.isEmpty()) {
			result = levenshteinSetMatcher.findMatch(entry, setConfig).getBestMatches();
		}
		if (result.isEmpty()) {
			return new ArrayList<>();
		}
		return result.stream().map(i -> i.target).collect(Collectors.toList());
	}

}
