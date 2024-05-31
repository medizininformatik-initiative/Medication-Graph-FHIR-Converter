package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.jetbrains.annotations.NotNull;

/**
 * Simple implementation of {@link InputUsageStatement} which provides a field for the original input.
 *
 * @author Markus Budeus
 */
abstract class AbstractUsageStatement<T> implements InputUsageStatement<T> {

	@NotNull
	private final T original;

	public AbstractUsageStatement(@NotNull T original) {
		this.original = original;
	}

	@Override
	@NotNull
	public T getOriginal() {
		return original;
	}
}
