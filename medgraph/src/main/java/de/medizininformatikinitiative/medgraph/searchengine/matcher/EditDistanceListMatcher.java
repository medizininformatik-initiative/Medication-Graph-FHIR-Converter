package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.EditDistanceService;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.EditDistance;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.InputUsageTraceable;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.IntRange;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

/**
 * This matcher matches search terms (as string lists) against identifiers in the following way: For the identifier, the
 * amount of tokens is counted (which we refer to as <b>n</b>). If the search term has fewer tokens, the match is
 * considered unsuccessful. Otherwise, each combination of <b>n</b> consecutive terms from the search term is taken and
 * joined usings spaces. Using the a given {@link EditDistanceService}, the resulting string is compared against the
 * identifier, whose terms are also joined together using spaces. If the edit distance provides a result, these terms
 * are considered a match.
 * <p>
 * Only a single list of consecutive terms is matched. If multiple matches can be found within the search term, the best
 * match (with the lowest Levenshtein distance) is used. If there are multiple equally good matches, the first one is
 * used.
 *
 * @author Markus Budeus
 */
public class EditDistanceListMatcher extends SimpleMatcher<List<String>, EditDistanceListMatcher.Match> {

	private final EditDistanceService editDistanceService;

	/**
	 * Creates a new {@link EditDistanceListMatcher}. Please not the given {@link EditDistanceService} should strongly
	 * limit what is considered a match, otherwise lots of matches will be generated. (I.e. whenever
	 * {@link EditDistanceService#apply(String, String)} returns a nonempty result, a match is generated)
	 *
	 * @param editDistanceService the edit distance service to use
	 */
	public EditDistanceListMatcher(@NotNull EditDistanceService editDistanceService) {
		this.editDistanceService = editDistanceService;
	}

	@Override
	protected Match match(List<String> searchTerm, MappedIdentifier<List<String>> mi) {
		String[] target = mi.identifier.getIdentifier().toArray(new String[0]);
		int tokens = target.length;

		if (tokens == 0) return null; // No. I won't match against an empty identifier.

		IntRange bestMatchRange = null;
		EditDistance bestMatchEditDistance = null;
		int bestDistance = Integer.MAX_VALUE;
		for (int start = 0; start <= searchTerm.size() - tokens; start++) {
			int end = start + tokens;
			String[] searchTermTokens = searchTerm.subList(start, end).toArray(new String[0]);
			EditDistance distance = getDistance(searchTermTokens, target);
			if (distance != null && distance.getEditDistance() < bestDistance) {
				bestDistance = distance.getEditDistance();
				bestMatchRange = new IntRange(start, end);
				bestMatchEditDistance = distance;
				if (bestDistance == 0) break; // No better result is possible, so we can abort
			}
		}

		if (bestMatchRange == null) return null;

		return new Match(mi, bestMatchEditDistance,
				new StringListUsageStatement(searchTerm, listIntegers(bestMatchRange)));
	}

	/**
	 * Joins the given terms using spaces and then calculates the Levenshtein distance between the given terms. Returns
	 * an {@link EditDistance} describing the result of null if the distance between the resulting strings exceeds the
	 * configured threshold.
	 */
	@Nullable
	private EditDistance getDistance(String[] searchTermTokens, String[] identifierTokens) {
		String searchTerm = String.join(" ", searchTermTokens);
		String identifier = String.join(" ", identifierTokens);
		OptionalInt distance = editDistanceService.apply(searchTerm, identifier);
		if (distance.isPresent()) {
			return new EditDistance(searchTerm, identifier, distance.getAsInt());
		}
		return null;
	}

	/**
	 * Returns a set of all integers which lie within the given range.
	 */
	private Set<Integer> listIntegers(IntRange range) {
		Set<Integer> result = new HashSet<>();
		for (int i = range.getFrom(); i < range.getTo(); i++) {
			result.add(i);
		}
		return result;
	}

	@Override
	protected boolean supportsParallelism() {
		return true;
	}

	public static class Match extends de.medizininformatikinitiative.medgraph.searchengine.matcher.model.Match<List<String>>
			implements InputUsageTraceable<StringListUsageStatement> {

		/**
		 * The {@link EditDistance}-instance with information about the matched parts' edit distance.
		 */
		private final EditDistance distance;
		/**
		 * The usage statements describing which part of the search tokens was used.
		 */
		private final StringListUsageStatement usageStatement;

		protected Match(MappedIdentifier<List<String>> matchedIdentifier, EditDistance distance,
		                StringListUsageStatement usageStatement) {
			super(matchedIdentifier);
			this.distance = distance;
			this.usageStatement = usageStatement;
		}

		public EditDistance getDistance() {
			return distance;
		}

		public StringListUsageStatement getUsageStatement() {
			return usageStatement;
		}
	}

}
