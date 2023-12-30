package de.tum.med.aiim.markusbudeus.matcher2.provider;

import de.tum.med.aiim.markusbudeus.matcher2.stringtransformer.Transformer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link IdentifierProvider} which copies the identifiers from the given provider and applies the transformation
 * given by the {@link Transformer} to them.
 * This class calculates the transformations of all identifiers immediately and only once.
 */
public class TransformedProvider<S, T> implements IdentifierProvider<T> {

	private final List<MappedIdentifier<T>> identifiers;

	public TransformedProvider(IdentifierProvider<S> base, Transformer<S, T> transformer) {
		this.identifiers = transform(base.getIdentifiers(), transformer);
	}

	private List<MappedIdentifier<T>> transform(List<MappedIdentifier<S>> sourceList, Transformer<S, T> transformer) {
		return sourceList
				.stream()
				.map(m -> new MappedIdentifier<>(transformer.transform(m.identifier), m.target))
				.collect(Collectors.toList());
	}

	@Override
	public List<MappedIdentifier<T>> getIdentifiers() {
		return identifiers;
	}
}
