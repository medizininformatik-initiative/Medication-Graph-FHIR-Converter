package de.tum.med.aiim.markusbudeus.matcher.provider;

import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.Transformer;

import java.util.HashMap;
import java.util.Map;

public class TransformedProvider<S, T> implements IdentifierProvider<T> {

	private final Map<T, MappedIdentifier<T>> synonymes;
	private final IdentifierProvider<S> base;
	private final Transformer<S, T> transformer;

	public TransformedProvider(IdentifierProvider<S> base, Transformer<S, T> transformer) {
		this.base = base;
		this.transformer = transformer;
		this.synonymes = transform(base.getIdentifiers(), transformer);
	}

	private Map<T, MappedIdentifier<T>> transform(Map<S, MappedIdentifier<S>> sourceMap, Transformer<S, T> transformer) {
		Map<T, MappedIdentifier<T>> result = new HashMap<>();
		sourceMap.forEach((name, synonyme) -> {
			T newName = transformer.transform(name);
			MappedIdentifier<T> target = result.computeIfAbsent(newName, n ->
					new MappedIdentifier<>(new TransformedIdentifier<>(newName, synonyme.identifier)));
			target.targets.addAll(synonyme.targets);
		});
		return result;
	}

	@Override
	public Map<T, MappedIdentifier<T>> getIdentifiers() {
		return synonymes;
	}

	@Override
	public T applyTransformation(String source) {
		return transformer.transform(base.applyTransformation(source));
	}
}
