package de.medizininformatikinitiative.medgraph.searchengine.model.identifier;

import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.Transformer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An {@link Identifier} that has been transformed by a {@link Transformer}.
 *
 * @author Markus Budeus
 */
public class TransformedIdentifier<S, T> extends Identifier<T> {

	/**
	 * The original identifier which was transformed into this one.
	 */
	@NotNull
	private final Identifier<S> original;
	/**
	 * The transformer which transformed the given original identifier into this one.
	 */
	@NotNull
	private final Transformer<S, T> transformer;

	public TransformedIdentifier(@NotNull T identifier, @NotNull Identifier<S> original, @NotNull Transformer<S, T> transformer) {
		super(identifier);
		this.original = original;
		this.transformer = transformer;
	}

	@NotNull
	public Identifier<S> getOriginal() {
		return original;
	}

	@NotNull
	public Transformer<S, T> getTransformer() {
		return transformer;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		if (!super.equals(object)) return false;
		TransformedIdentifier<?, ?> that = (TransformedIdentifier<?, ?>) object;
		return Objects.equals(original, that.original) && Objects.equals(transformer, that.transformer);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), original, transformer);
	}
}
