package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.Transformer;

/**
 * {@link IdentifierProvider} which exposes functions to design a stream pipeline which is built when
 * {@link #getIdentifiers()} is called. Uses {@link MappedIdentifier}s only. As opposed to using
 * {@link java.util.stream.Stream} directly, the generated pipeline can be reused as much as you want.
 *
 * @author Markus Budeus
 */
public interface MappedIdentifierStream<S, T extends Identifiable> extends IdentifierProvider<MappedIdentifier<S, T>> {

	/**
	 * Returns an {@link MappedIdentifierStream} with parallel processing enabled for the stream.
	 */
	default MappedIdentifierStream<S, T> parallel() {
		return new ParallelMappedIdentifierStream<>(this);
	}

	/**
	 * Returns an {@link MappedIdentifierStream} with the given transformer applied to the processed entries.
	 */
	default <Q> MappedIdentifierStream<Q, T> withTransformation(Transformer<S, Q> transformer) {
		return new TransformedMappedIdentifierStream<>(this, transformer);
	}

	/**
	 * Returns an {@link MappedIdentifierStream} which eagerly processes the current stream pipeline. It stores the
	 * results, meaning multiple calls to {@link #getIdentifiers()} on an eager stream will not cause the previous
	 * pipeline to be processed multiple times.
	 */
	default MappedIdentifierStream<S, T> eager() {
		return new EagerIdentiferStream<>(this);
	}

}
