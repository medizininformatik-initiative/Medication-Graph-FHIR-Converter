package de.medizininformatikinitiative.medgraph.searchengine.matcher.model;

import org.jetbrains.annotations.NotNull;

/**
 * Specifies two strings and an edit distance between them. This class is immutable.
 *
 * @author Markus Budeus
 */
public record EditDistance(@NotNull String value1, @NotNull String value2, int editDistance) implements Comparable<EditDistance> {

	@Override
	public String toString() {
		return "'" + value1 + "' --(distance " + editDistance + ")-> '" + value2 + "'";
	}

	@Override
	public int compareTo(@NotNull EditDistance o) {
		return Double.compare(editDistance, o.editDistance);
	}
}
