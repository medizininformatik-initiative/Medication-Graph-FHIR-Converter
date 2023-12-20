package de.tum.med.aiim.markusbudeus.matcher.algorithms;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.OngoingMatching;
import de.tum.med.aiim.markusbudeus.matcher.identifiermatcher.*;
import de.tum.med.aiim.markusbudeus.matcher.provider.BaseProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.ListToSet;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.ToLowerCase;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.TrimSeparatorSigns;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.WhitespaceTokenizer;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

public class SampleAlgorithm implements MatchingAlgorithm {

	private final BaseProvider baseProvider = BaseProvider.ofDatabaseSynonymes();
	private final ExactMatcher exactMatcher;
	private final JaccardMatcher jaccardMatcher;
	private final LevenshteinMatcher levenshteinMatcher;
	private final LevenshteinSetMatcher levenshteinSetMatcher;

	public SampleAlgorithm() {
		IdentifierProvider<String> lowerCaseProvider = baseProvider.transform(new ToLowerCase());
		IdentifierProvider<Set<String>> setMatcherProvider = lowerCaseProvider
				.transform(new WhitespaceTokenizer())
				.transform(new TrimSeparatorSigns())
				.transform(new ListToSet());
		jaccardMatcher = new JaccardMatcher(setMatcherProvider);
		levenshteinSetMatcher = new LevenshteinSetMatcher(setMatcherProvider);
		exactMatcher = new ExactMatcher(lowerCaseProvider);
		levenshteinMatcher = new LevenshteinMatcher(lowerCaseProvider);
	}

	@Override
	public List<IdentifierTarget> match(HouselistEntry entry) {
		OngoingMatching matching = new OngoingMatching(entry, baseProvider);

		doMatchingSteps(matching);

		return Tools.sortDeterministically(matching.getCurrentMatches());
	}

	private void doMatchingSteps(OngoingMatching matching) {
		if (narrowDownUnlessEmpty(matching, exactMatcher))
			return;
		if (narrowDownUnlessEmpty(matching, jaccardMatcher))
			return;
		if (narrowDownUnlessEmpty(matching, levenshteinMatcher))
			return;
		matching.narrowDown(levenshteinSetMatcher.findMatch(matching.entry.name).getBestMatches());
	}

	private boolean narrowDownUnlessEmpty(OngoingMatching matching, IIdentifierMatcher<?> matcher) {
		return matching.narrowDownUnlessEmpty(matcher.findMatch(matching.entry.name).getBestMatches());
	}

}
