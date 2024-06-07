package de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance;

import java.util.OptionalInt;

/**
 * Interface which provides calculation of an edit distance between two strings. The exact edit distance metric to be
 * used as well as possible thresholds is left to the implementation.
 *
 * @author Markus Budeus
 */
public interface EditDistanceService {

	/**
	 * Calculates an edit distance between the two strings. If no edit distance can be calculated, (e.g. because it
	 * exceeds an implementation-defined limit), an empty optional is returned.
	 * @param left the first word
	 * @param right the second word
	 * @return an {@link OptionalInt} holding the calculated edit distance of an empty {@link OptionalInt} is the
	 * edit distance was not calculated successfully.
	 */
	OptionalInt apply(String left, String right);

}
