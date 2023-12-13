package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.BaseProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.Identifier;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.ToLowerCase;
import de.tum.med.aiim.markusbudeus.matcher.stringmatcher.ExactMatcher;
import de.tum.med.aiim.markusbudeus.matcher.stringmatcher.IStringMatcher;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Main {

	public static void main(String[] args) throws IOException {
		IdentifierProvider<String> provider = BaseProvider.instantiate()
				.transform(new ToLowerCase());
		IStringMatcher<String> matcher = new ExactMatcher(provider);

		List<HouselistEntry> entries = HouselistMatcher.loadHouselist();
		Stream<MatchingResult<HouselistEntry, String>> resultStream = performMatching(entries,
				houselistEntry -> houselistEntry.noisySubstanceName,
				matcher);

		resultStream.forEach(result -> {
			System.out.println(
					result.searchTerm.noisySubstanceName + " (" + result.searchTerm.substanceName + ") -> " +result.result.getBestMatches());
		});
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
