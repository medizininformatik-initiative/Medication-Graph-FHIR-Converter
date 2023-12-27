package de.tum.med.aiim.markusbudeus.matcher.algorithms;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.OngoingMatching;
import de.tum.med.aiim.markusbudeus.matcher.houselisttransformer.DosageFromNameIdentifier;
import de.tum.med.aiim.markusbudeus.matcher.identifiermatcher.*;
import de.tum.med.aiim.markusbudeus.matcher.provider.BaseProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.DosageFilter;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.ProductOnlyFilter;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.SubstanceToProductResolver;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.ListToSet;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.ToLowerCase;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.TrimSpecialSuffixSymbols;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.WhitespaceTokenizer;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.Set;

public class SampleAlgorithm implements MatchingAlgorithm {

	private final BaseProvider baseProvider = BaseProvider.ofDatabaseSynonymes();
	private final ExactMatcher exactMatcher;
	private final UnionSizeMatcher unionSizeMatcher;
	private final LevenshteinMatcher levenshteinMatcher;
	private final LevenshteinSetMatcher levenshteinSetMatcher;

	private final DosageFromNameIdentifier dosageFromNameIdentifier;
	private final DosageFilter dosageFilter;
	private final SubstanceToProductResolver substanceToProductResolver;
	private final ProductOnlyFilter productOnlyFilter;

	public SampleAlgorithm(Session session) {
		IdentifierProvider<String> lowerCaseProvider = baseProvider.transform(new ToLowerCase());
		IdentifierProvider<Set<String>> setMatcherProvider = lowerCaseProvider
				.transform(new WhitespaceTokenizer())
				.transform(new TrimSpecialSuffixSymbols())
				.transform(new ListToSet());
		unionSizeMatcher = new UnionSizeMatcher(setMatcherProvider);
		levenshteinSetMatcher = new LevenshteinSetMatcher(setMatcherProvider);
		exactMatcher = new ExactMatcher(lowerCaseProvider);
		levenshteinMatcher = new LevenshteinMatcher(lowerCaseProvider);

		dosageFromNameIdentifier = new DosageFromNameIdentifier();
		dosageFilter = new DosageFilter(session);
		substanceToProductResolver = new SubstanceToProductResolver(session);
		productOnlyFilter = new ProductOnlyFilter();
	}

	@Override
	public List<IdentifierTarget> match(HouselistEntry entry) {
		OngoingMatching matching = new OngoingMatching(entry, baseProvider);

		doMatchingSteps(matching);

		dosageFromNameIdentifier.transform(matching.entry);

		// Only use product matches, unless this leaves us without a result. In that case, transform substances
		// to products.
		if (!matching.transformResults(productOnlyFilter, true)) {
			matching.transformResults(substanceToProductResolver);

			// Since we now resolved substances into products, we once again prefer products whose name also
			// matches the search term.
			narrowDownUnlessEmpty(matching, unionSizeMatcher);
		}
		matching.transformResults(dosageFilter, true);

		return Tools.sortDeterministically(matching.getCurrentMatches());
	}

	private void doMatchingSteps(OngoingMatching matching) {
		if (narrowDownUnlessEmpty(matching, exactMatcher))
			return;
		if (narrowDownUnlessEmpty(matching, unionSizeMatcher))
			return;
		if (narrowDownUnlessEmpty(matching, levenshteinMatcher))
			return;
		matching.narrowDown(levenshteinSetMatcher.findMatch(matching.entry.name).getBestMatches());
	}

	private boolean narrowDownUnlessEmpty(OngoingMatching matching, IIdentifierMatcher<?> matcher) {
		return matching.narrowDownUnlessEmpty(matcher.findMatch(matching.entry.name).getBestMatches());
	}

}
