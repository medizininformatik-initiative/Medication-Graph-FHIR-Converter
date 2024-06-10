package de.medizininformatikinitiative.medgraph.searchengine.matcher.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Specifies two strings and an edit distance between them. This class is immutable.
 *
 * @author Markus Budeus
 */
public class EditDistance {

	@NotNull
	private final String value1;
	@NotNull
	private final String value2;
	private final int editDistance;

	public EditDistance(@NotNull String value1, @NotNull String value2, int editDistance) {
		this.value1 = value1;
		this.value2 = value2;
		this.editDistance = editDistance;
	}

	@NotNull
	public String getValue1() {
		return value1;
	}

	@NotNull
	public String getValue2() {
		return value2;
	}

	public int getEditDistance() {
		return editDistance;
	}

	@Override
	public String toString() {
		return "'"+value1+"' --(distance "+editDistance+")-> '"+value2+"'";
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		EditDistance that = (EditDistance) object;
		return editDistance == that.editDistance && Objects.equals(value1,
				that.value1) && Objects.equals(value2, that.value2);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value1, value2, editDistance);
	}
}
