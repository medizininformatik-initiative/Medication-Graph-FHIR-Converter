//package de.medizininformatikinitiative.medgraph.searchengine.matcher.model;
//
//import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TrackableIdentifier;
//import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
//import org.jetbrains.annotations.NotNull;
//
///**
// * A match which has a given score based on whatever metric the corresponding matcher uses when comparing it with the
// * given search term.
// *
// * @author Markus Budeus
// */
//public class ScoreBasedMatch<S, T> extends Match<S, T> implements Comparable<ScoreBasedMatch<?, ?>> {
//	private final double score;
//
//	/**
//	 * Creates a new score based match.
//	 *
//	 * @param matchedIdentifier the matched identifier
//	 * @param score             the score assigned to the identifier according to the matcher's metric
//	 */
//	public ScoreBasedMatch(TrackableIdentifier<S> searchTerm, MappedIdentifier<T> matchedIdentifier, double score) {
//		super(searchTerm, matchedIdentifier);
//		this.score = score;
//	}
//
//	/**
//	 * Returns the score assigned to this match.
//	 */
//	public double getScore() {
//		return score;
//	}
//
//	@Override
//	public int compareTo(@NotNull ScoreBasedMatch<?,?> o) {
//		return Double.compare(score, o.score);
//	}
//}
