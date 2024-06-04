package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.IntRange;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.MultiSubstringUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * This class provides functionality to parse a string query step-by-step. Which means, you provide the string to parse
 * up-front and subsequently defer parsing to another component using {@link #useRemainingQueryParts(Function)}. The
 * passed function receives the query as parameter and returns a usage statement for the query. This usage statement is
 * then applied and during the next invocation of {@link #useRemainingQueryParts(Function)}, only the previously unused
 * parts of the query are passed to the function. Then once again, the returned usage statement is applied to the query,
 * causing the third invocation of {@link #useRemainingQueryParts(Function)} to only receive the parts of the query
 * which were not used by any previous invocation.
 *
 * @author Markus Budeus
 */
public class StepwiseGeneralQueryParser {

	private DistinctMultiSubstringUsageStatement queryUsageStatement;

	/**
	 * Creates a new stepwise parser.
	 *
	 * @param query the full query to parse
	 */
	public StepwiseGeneralQueryParser(String query) {
		queryUsageStatement = new DistinctMultiSubstringUsageStatement(query, Collections.emptySet());
	}

	/**
	 * Invokes the given action, passing whatever parts of the query have not been used by previous invocations of this
	 * function. The action must return a {@link SubstringUsageStatement} for the string that was passed to it,
	 * indicating which parts of the query were consumed by the action. Those parts are eliminated from the original,
	 * full-sized query maintained by this instance. Then, the next invocation of this function causes the already
	 * consumed parts of the query to no longer be passed to the action.
	 * <p>
	 * Thus, through repeatedly invoking this function, you can "use up" each part of the original query using different
	 * consumers.
	 * <p>
	 * The usage statement returned by this function is the usage statement returned by the action, but remapped to the
	 * original query. For example:<br> The original query string is "Earth Wind Fire". During the first invocation, the
	 * action is passed this query string and returns a usage statement indicating it used the part "Wind ".<br> During
	 * the next invocation, the function is passed the remaining query, which is "Earth Fire". It returns a usage
	 * statement which indicates it used the part "Fire" from "Earth Fire". In that case, the return value of this
	 * function is a usage statement which indicates that the part "Fire" (region [6:10]) from the original string
	 * "Earth Wind Fire" was used. (I.e. region [11:15]).
	 *
	 * @param action the action to invoke, which is passed the currently unsued parts of the query and must return a
	 *               usage statement for those parts
	 * @return the usage statement as returned by the function, but remapped to reflect what parts of the original,
	 * full-sized query have been used.
	 * @throws IllegalStateException if the action returns a usage statement which does not have the value passed the
	 *                               action as its original value
	 * @throws NullPointerException  if action is null or it returns a null usage statement
	 */
	public SubstringUsageStatement useRemainingQueryParts(Function<String, SubstringUsageStatement> action) {
		SubstringUsageStatement usageStatement = action.apply(queryUsageStatement.getUnusedParts());
		MultiSubstringUsageStatement primaryQueryUsageStatement;
		try {
			primaryQueryUsageStatement = remapSecondaryUsageToPrimaryUsage(queryUsageStatement, usageStatement);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(
					"The usage statement returned by the function does not specify the provided function input as it's original value! " +
							"The usage has not been applied to the query!");
		}
		this.queryUsageStatement = merge(queryUsageStatement, primaryQueryUsageStatement).distinct();
		return primaryQueryUsageStatement;
	}

	/**
	 * Returns the original query of this query parser.
	 */
	public String getOriginalQuery() {
		return queryUsageStatement.getOriginal();
	}

	/**
	 * Returns a usage statement indicating which parts of the original query have already been consumed by previous
	 * invocations of {@link #useRemainingQueryParts(Function)}.
	 */
	public DistinctMultiSubstringUsageStatement getQueryUsageStatement() {
		return queryUsageStatement;
	}

	/**
	 * Assumes the secondaryUsage is a usage statement for the unused parts of primaryUsage, this function remaps the
	 * secondaryUsage to use the primary usage's original value as its original.
	 * <p>
	 * For example: primaryUsage is a usage statement with the original string "Earth Wind Fire" and uses the region
	 * [6:11], meaning its unused part is "Earth Fire".<br> secondaryUsage has the original "Earth Fire" (if it does not
	 * an {@link IllegalArgumentException} is thrown) and uses the region [6:10] (i.e. "Fire").<br> Then, this function
	 * returns a usage statement whose original is "Earth Wind Fire" and whose region is [11:15] (i.e. "Fire").
	 */
	private MultiSubstringUsageStatement remapSecondaryUsageToPrimaryUsage(
			DistinctMultiSubstringUsageStatement primaryUsage, SubstringUsageStatement secondaryUsage) {
		if (!primaryUsage.getUnusedParts().equals(secondaryUsage.getOriginal())) {
			throw new IllegalArgumentException(
					"The secondary usage statement must use whatever parts from the primary " +
							"usage statement are unused as its original!");
		}
		List<IntRange> sortedPrimaryRanges = primaryUsage.getUsedRangesAsList();
		Set<IntRange> mappedSecondaryRanges = new HashSet<>();
		for (IntRange range : secondaryUsage.getUsedRanges()) {
			int from = range.getFrom();
			int offsetInPrimary = 0;
			for (IntRange primaryRange : sortedPrimaryRanges) {
				if (from + offsetInPrimary < primaryRange.getFrom()) {
					break;
				}
				offsetInPrimary += primaryRange.getSize();
			}
			mappedSecondaryRanges.add(new IntRange(from + offsetInPrimary, range.getTo() + offsetInPrimary));
		}

		return new MultiSubstringUsageStatement(primaryUsage.getOriginal(), mappedSecondaryRanges);
	}

	/**
	 * Merges the given usage statements, creating a substring usage statement which uses all regions of the original
	 * strings which either of the given statements do.
	 *
	 * @throws IllegalArgumentException if the two statements do not have the same original string
	 */
	private MultiSubstringUsageStatement merge(SubstringUsageStatement statement1, SubstringUsageStatement statement2) {
		if (!statement1.getOriginal().equals(statement2.getOriginal())) {
			throw new IllegalArgumentException(
					"You can only merge two usage statements which use the same original strings!");
		}
		Set<IntRange> ranges = new HashSet<>();
		ranges.addAll(statement1.getUsedRanges());
		ranges.addAll(statement2.getUsedRanges());
		return new MultiSubstringUsageStatement(statement1.getOriginal(), ranges);
	}

}
