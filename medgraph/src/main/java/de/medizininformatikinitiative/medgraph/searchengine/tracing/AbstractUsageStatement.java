package de.medizininformatikinitiative.medgraph.searchengine.tracing;

/**
 * @author Markus Budeus
 */
abstract class AbstractUsageStatement<T> implements InputUsageStatement<T> {

	// TODO Javadoc

	private final T input;

	public AbstractUsageStatement(T input) {
		this.input = input;
	}

	@Override
	public T getInput() {
		return input;
	}
}
