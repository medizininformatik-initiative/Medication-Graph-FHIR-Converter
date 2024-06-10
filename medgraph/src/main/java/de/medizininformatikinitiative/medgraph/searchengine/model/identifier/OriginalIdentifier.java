package de.medizininformatikinitiative.medgraph.searchengine.model.identifier;

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
	private final Source source;

	public OriginalIdentifier(T identifier, Source source) {
		super(identifier);
		this.source = source;
	}

	public Source getSource() {
		return source;
	}

	public enum Source {
		/**
		 * This identifier is a known identifier from the knowledge graph.
		 */
		KNOWN_IDENTIFIER,
		/**
		 * This identifier was taken from the search query provided by the user.
		 */
		USER_SEARCH
	}

}
