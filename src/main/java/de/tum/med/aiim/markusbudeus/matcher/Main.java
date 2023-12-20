package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.BaseProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher.identifiermatcher.JaccardMatcher;
import de.tum.med.aiim.markusbudeus.matcher.sample.synthetic.HouselistMatcher;
import de.tum.med.aiim.markusbudeus.matcher.sample.synthetic.SyntheticHouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.ListToSet;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.ToLowerCase;
import de.tum.med.aiim.markusbudeus.matcher.identifiermatcher.IIdentifierMatcher;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.WhitespaceTokenizer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public class Main {

	public static void main(String[] args) throws IOException {
		IdentifierProvider<Set<String>> provider = BaseProvider
				.ofDatabaseSynonymes()
				.transform(new ToLowerCase())
				.transform(new WhitespaceTokenizer())
				.transform(new ListToSet());

		makeMatchingRun(
				provider,
				JaccardMatcher::new
		);
	}

	public static <S, T> void makeMatchingRun(
			IdentifierProvider<S> provider,
			Function<IdentifierProvider<S>, IIdentifierMatcher<S>> matcherConstructor)
	throws IOException {

		IIdentifierMatcher<S> matcher = matcherConstructor.apply(provider);

		List<SyntheticHouselistEntry> entries = HouselistMatcher.loadHouselist();
		Stream<MatchingResult<SyntheticHouselistEntry, S>> resultStream = performMatching(entries,
				houselistEntry -> houselistEntry.noisySubstanceName,
				matcher);

		AtomicInteger total = new AtomicInteger();
		AtomicInteger unique = new AtomicInteger();
		AtomicInteger ambiguous = new AtomicInteger();
		AtomicInteger unmatched = new AtomicInteger();
		resultStream.forEach(result -> {
			total.getAndIncrement();
			Set<MappedIdentifier<S>> bestMatches = result.result.getBestMatches();

			if (bestMatches.size() == 1 && bestMatches.iterator().next().targets.size() == 1) {
				unique.getAndIncrement();
			} else if (bestMatches.isEmpty()) {
				unmatched.getAndIncrement();
			} else {
				ambiguous.getAndIncrement();
			}

			System.out.println(
					result.searchTerm.noisySubstanceName + " (" + result.searchTerm.substanceName + ") -> " + bestMatches);
		});

		DecimalFormat f = new DecimalFormat("0.0");
		System.out.println("Total of "+total.get()+" entries.");
		System.out.println(unique.get() + " unique matches (" + f.format(100.0 * unique.get() / total.get()) + "%)");
		System.out.println(ambiguous.get() + " ambiguous matches (" + f.format(100.0 * ambiguous.get() / total.get()) + "%)");
		System.out.println(unmatched.get() + " unmatched (" + f.format(100.0 * unmatched.get() / total.get()) + "%)");
	}

	public static <T, S> Stream<MatchingResult<T, S>> performMatching(List<T> searchTerms,
	                                                                  Function<T, String> searchTermMapper,
	                                                                  IIdentifierMatcher<S> matcher) {
		return performMatching(searchTerms.stream(), searchTermMapper, matcher);
	}

	public static <T, S> Stream<MatchingResult<T, S>> performMatching(Stream<T> searchTerms,
	                                                                  Function<T, String> searchTermMapper,
	                                                                  IIdentifierMatcher<S> matcher) {
		return performMatchingWithoutPreTransform(searchTerms,
				t -> matcher.transform(searchTermMapper.apply(t)),
				matcher);
	}

	public static <T, S> Stream<MatchingResult<T, S>> performMatchingWithoutPreTransform(Stream<T> searchTerms,
	                                                                                     Function<T, S> searchTermMapper,
	                                                                                     IIdentifierMatcher<S> matcher) {
		return searchTerms.map(term -> new MatchingResult<>(term, matcher.findMatch(searchTermMapper.apply(term))));
	}

}
