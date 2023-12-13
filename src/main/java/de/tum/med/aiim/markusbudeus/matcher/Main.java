package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.BaseProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.Identifier;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher.stringmatcher.JaccardMatcher;
import de.tum.med.aiim.markusbudeus.matcher.transformer.ListToSet;
import de.tum.med.aiim.markusbudeus.matcher.transformer.ToLowerCase;
import de.tum.med.aiim.markusbudeus.matcher.stringmatcher.IStringMatcher;
import de.tum.med.aiim.markusbudeus.matcher.transformer.WhitespaceTokenizer;

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
				.instantiate()
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
			Function<IdentifierProvider<S>, IStringMatcher<S>> matcherConstructor)
	throws IOException {

		IStringMatcher<S> matcher = matcherConstructor.apply(provider);

		List<HouselistEntry> entries = HouselistMatcher.loadHouselist();
		Stream<MatchingResult<HouselistEntry, S>> resultStream = performMatching(entries,
				houselistEntry -> houselistEntry.noisySubstanceName,
				matcher);

		AtomicInteger total = new AtomicInteger();
		AtomicInteger unique = new AtomicInteger();
		AtomicInteger ambiguous = new AtomicInteger();
		AtomicInteger unmatched = new AtomicInteger();
		resultStream.forEach(result -> {
			total.getAndIncrement();
			Set<Identifier<S>> bestMatches = result.result.getBestMatches();

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
	                                                                  IStringMatcher<S> matcher) {
		return performMatching(searchTerms.stream(), searchTermMapper, matcher);
	}

	public static <T, S> Stream<MatchingResult<T, S>> performMatching(Stream<T> searchTerms,
	                                                                  Function<T, String> searchTermMapper,
	                                                                  IStringMatcher<S> matcher) {
		return performMatchingWithoutPreTransform(searchTerms,
				t -> matcher.transform(searchTermMapper.apply(t)),
				matcher);
	}

	public static <T, S> Stream<MatchingResult<T, S>> performMatchingWithoutPreTransform(Stream<T> searchTerms,
	                                                                                     Function<T, S> searchTermMapper,
	                                                                                     IStringMatcher<S> matcher) {
		return searchTerms.map(term -> new MatchingResult<>(term, matcher.findMatch(searchTermMapper.apply(term))));
	}

}
