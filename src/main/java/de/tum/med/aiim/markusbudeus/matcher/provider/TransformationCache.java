package de.tum.med.aiim.markusbudeus.matcher.provider;

import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.Transformer;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches the applied transformations so they are not recomputed every time the same transformation gets used.
 */
public class TransformationCache<S> implements IdentifierProvider<S> {

	private final IdentifierProvider<S> base;

	private final Map<Transformer<?,?>, IdentifierProvider<?>> cache = new HashMap<>();

	public TransformationCache(IdentifierProvider<S> base) {
		this.base = base;
	}


	@Override
	public Map<S, MappedIdentifier<S>> getIdentifiers() {
		return base.getIdentifiers();
	}

	@Override
	public <T> IdentifierProvider<T> transform(Transformer<S, T> transformer) {
		//noinspection unchecked
		return (IdentifierProvider<T>) cache.computeIfAbsent(transformer, t -> base.transform(transformer));
	}

	@Override
	public S applyTransformation(String source) {
		return base.applyTransformation(source);
	}
}
