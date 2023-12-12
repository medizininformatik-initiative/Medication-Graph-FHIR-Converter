package de.tum.med.aiim.markusbudeus.matcher;

import java.util.Objects;

public class SubstanceMatch {

	public final long mmiId;
	public final String name;

	public SubstanceMatch(long mmiId, String name) {
		this.mmiId = mmiId;
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SubstanceMatch that = (SubstanceMatch) o;
		return mmiId == that.mmiId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(mmiId);
	}

}
