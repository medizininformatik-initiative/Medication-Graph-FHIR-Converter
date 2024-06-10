package de.medizininformatikinitiative.medgraph.searchengine.model.identifier;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An original identifier which came from an external source, like user input or the knowledge graph.
 *
 * @param <T> the type of the identifier this instance wraps
 * @author Markus Budeus
 */
public class OriginalIdentifier<T> extends Identifier<T> {

	/**
	 * Where this identifier originated from.
	 */
	@NotNull
	private final Source source;

	public OriginalIdentifier(@NotNull T identifier, @NotNull Source source) {
		super(identifier);
		this.source = source;
	}

	@NotNull
	public Source getSource() {
		return source;
	}

	public enum Source {
		/**
		 * This identifier is a known identifier from the knowledge graph.
		 */
		KNOWN_IDENTIFIER,
		/**
		 * This identifier was taken from the raw search query provided by the user.
		 */
		RAW_QUERY,
		/**
		 * This identifier was taken from the refined search query.
		 */
		SEARCH_QUERY
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		if (!super.equals(object)) return false;
		OriginalIdentifier<?> that = (OriginalIdentifier<?>) object;
		return source == that.source;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), source);
	}
}
