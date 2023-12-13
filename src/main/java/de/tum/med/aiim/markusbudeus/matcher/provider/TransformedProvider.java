package de.tum.med.aiim.markusbudeus.matcher.provider;

import de.tum.med.aiim.markusbudeus.matcher.transformer.Transformer;

import java.util.HashMap;
import java.util.Map;

public class TransformedProvider<S, T> implements IdentifierProvider<T> {

	private final Map<T, Identifier<T>> synonymes;
	private final IdentifierProvider<S> base;
	private final Transformer<S, T> transformer;

	public TransformedProvider(IdentifierProvider<S> base, Transformer<S, T> transformer) {
		this.base = base;
		this.transformer = transformer;
		this.synonymes = transform(base.getIdentifiers(), transformer);
	}

	private Map<T, Identifier<T>> transform(Map<S, Identifier<S>> sourceMap, Transformer<S, T> transformer) {
		Map<T, Identifier<T>> result = new HashMap<>();
		sourceMap.forEach((name, synonyme) -> {
			T newName = transformer.transform(name);
			Identifier<T> target = result.computeIfAbsent(newName, Identifier::new);
			target.targets.addAll(synonyme.targets);
		});
		return result;
	}

	@Override
	public Map<T, Identifier<T>> getIdentifiers() {
		return synonymes;
	}

	@Override
	public T applyTransformation(String source) {
		return transformer.transform(base.applyTransformation(source));
	}
}
