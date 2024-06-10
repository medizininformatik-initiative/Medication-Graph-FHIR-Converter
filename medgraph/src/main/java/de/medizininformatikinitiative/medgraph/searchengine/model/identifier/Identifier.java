package de.medizininformatikinitiative.medgraph.searchengine.model.identifier;

/**
 * Wraps an identifier (i.e. name) of something. Usually a string that may or may not have been subjected through
 * various transformations. This wrapper can then hold information about where the (actual) identifier came from.
 * Instances of this class are immutable.
 *
 * @param <T> the type of this identifier
 * @author Markus Budeus
 */
public abstract class Identifier<T> {

	/**
	 * The actual identifier wrapped by this instance.
	 */
	private final T identifier;

	protected Identifier(T identifier) {
		this.identifier = identifier;
	}

	public T getIdentifier() {
		return identifier;
	}
}
