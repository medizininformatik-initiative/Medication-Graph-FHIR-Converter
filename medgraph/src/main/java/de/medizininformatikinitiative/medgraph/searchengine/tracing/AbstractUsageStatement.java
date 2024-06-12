package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		AbstractUsageStatement<?> that = (AbstractUsageStatement<?>) object;
		return Objects.equals(original, that.original);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(original);
	}

	@Override
	@NotNull
	public T getOriginal() {
		return original;
	}
}
