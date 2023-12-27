package de.tum.med.aiim.markusbudeus.matcher.provider;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class IdentifierTarget {

	public final long mmiId;
	public final String name;
	public final Type type;
	public final Set<Flag> flags = new HashSet<>();

	public IdentifierTarget(long mmiId, String name, Type type) {
		this.mmiId = mmiId;
		this.name = name;
		this.type = type;
	}

	public enum Type {
		PRODUCT,
		SUBSTANCE
	}

	public enum Flag {
		RESOLVED_VIA_SUBSTANCE
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IdentifierTarget that = (IdentifierTarget) o;
		return mmiId == that.mmiId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(mmiId);
	}

	@Override
	public String toString() {
		return name + " ("+type+")";
	}
}
