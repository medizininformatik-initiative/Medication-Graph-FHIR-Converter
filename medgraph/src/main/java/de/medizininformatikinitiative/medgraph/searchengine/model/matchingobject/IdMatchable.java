package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import java.util.Objects;

/**
 * A matching target which refers to an entity which carries a unique id for its type.
 */
public abstract class IdMatchable implements Matchable {

	protected final long id;
	protected final String name;

	public IdMatchable(long id, String name) {
		this.name = name;
		this.id = id;
	}

	public long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IdMatchable that = (IdMatchable) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}
