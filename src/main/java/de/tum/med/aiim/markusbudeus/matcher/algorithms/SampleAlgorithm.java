package de.tum.med.aiim.markusbudeus.matcher.algorithms;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.OngoingMatching;
import de.tum.med.aiim.markusbudeus.matcher.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher.houselisttransformer.DosageFromNameIdentifier;
import de.tum.med.aiim.markusbudeus.matcher.matchers.*;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.provider.BaseProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;
import de.tum.med.aiim.markusbudeus.matcher.provider.TransformedProvider;
import de.tum.med.aiim.markusbudeus.matcher.resultranker.DosageMatchJudge;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.DosageFilter;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.ProductOnlyFilter;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.SubstanceToProductResolver;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.*;
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
	private final DosageMatchJudge dosageMatchJudge;
	private final SubstringPresenceMatcher substringPresenceMatcher;

	public SampleAlgorithm(Session session) {
		baseProvider = BaseProvider.ofDatabaseSynonymes(session);
		unionSizeMatcher = new UnionSizeMatcher();
		levenshteinSetMatcher = new LevenshteinSetMatcher();
		levenshteinMatcher = new LevenshteinMatcher();
		substringPresenceMatcher = new SubstringPresenceMatcher();

		dosageFromNameIdentifier = new DosageFromNameIdentifier();
		dosageFilter = new DosageFilter(session);
		substanceToProductResolver = new SubstanceToProductResolver(session);
		productOnlyFilter = new ProductOnlyFilter();
		dosageMatchJudge = new DosageMatchJudge(session);
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
		matching.applySortingStep("Dosage Match Score", dosageMatchJudge, null);

		sortBySubsequencesFound(matching);

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

	private void sortBySubsequencesFound(OngoingMatching matching) {
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
