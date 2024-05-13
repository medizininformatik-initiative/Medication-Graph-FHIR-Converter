package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.Transformer;

/**
 * {@link IdentifierProvider} which exposes functions to design a stream pipeline which is built when
 * {@link #getIdentifiers()} is called. As opposed to using {@link java.util.stream.Stream} directly, the
 * generated pipeline can be reused as much as you want.
 *
 * @author Markus Budeus
 */
public interface IdentifierStream<S> extends IdentifierProvider<S> {

	/**
	 * Returns an {@link IdentifierStream} with parallel processing enabled for the stream.
	 */
	default IdentifierStream<S> parallel() {
		return new ParallelIdentifierStream<>(this);
	}

	/**
	 * Returns an {@link IdentifierStream} with the given transformer applied to the processed entries.
	 */
	default <T> IdentifierStream<T> withTransformation(Transformer<S, T> transformer) {
		return new TransformedIdentifierStream<>(this, transformer);
	}

	/**
	 * Returns an {@link IdentifierStream} which eagerly processes the current stream pipeline. It stores the results,
	 * meaning multiple calls to {@link #getIdentifiers()} on an eager stream will not cause the previous pipeline to
	 * be processed multiple times.
	 */
	default IdentifierStream<S> eager() {
		return new EagerIdentiferStream<>(this);
	}

}
