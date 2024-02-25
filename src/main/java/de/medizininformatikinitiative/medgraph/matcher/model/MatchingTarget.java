package de.medizininformatikinitiative.medgraph.matcher.model;

import java.util.Objects;

/**
 * An intermediate matching target.
 *
 * @author Markus Budeus
 */
public abstract class MatchingTarget {

	protected final long mmiId;
	protected final String name;

	public MatchingTarget(long mmiId, String name) {
		this.mmiId = mmiId;
		this.name = name;
	}

	public enum Type {
		PRODUCT,
		SUBSTANCE
	}

	public abstract Type getType();

	public long getMmiId() {
		return mmiId;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MatchingTarget that = (MatchingTarget) o;
		return mmiId == that.mmiId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(mmiId);
	}

	@Override
	public String toString() {
		return name + " ("+getType()+")";
	}

}
