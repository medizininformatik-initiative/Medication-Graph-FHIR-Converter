package de.tum.med.aiim.markusbudeus.matcher.provider;

import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.Transformer;

import java.util.Map;

public interface IdentifierProvider<S> {

	Map<S, MappedIdentifier<S>> getIdentifiers();

	default <T> IdentifierProvider<T> transform(Transformer<S,T> transformer) {
		return new TransformedProvider<>(this, transformer);
	}

	/**
	 * Applies all name transformations used by this provider to the given source string.
	 */
	S applyTransformation(String source);

}
