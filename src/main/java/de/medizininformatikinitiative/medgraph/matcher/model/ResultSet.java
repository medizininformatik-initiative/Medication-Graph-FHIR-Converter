package de.medizininformatikinitiative.medgraph.matcher.model;

import java.util.List;

/**
 * Results of a search. Contains a single (or no) primary result and sets of secondary and tertiary results.
 * The secondary results are those which matched equally well as the primary results, but which were not picked as
 * the primary result. Tertiary results are considered to match less well than secondary results, but still good enough
 * to not be eliminated.
 *
 * @param <T> the type of objects in this result set
 */
public class ResultSet<T extends ProductWithPzn> {
	public final T primaryResult;
	public final List<T> secondaryResults;
	public final List<T> tertiaryResults;

	public ResultSet(T primaryResult, List<T> secondaryResults,
	                 List<T> tertiaryResults) {
		this.primaryResult = primaryResult;
		this.secondaryResults = secondaryResults;
		this.tertiaryResults = tertiaryResults;
	}
}