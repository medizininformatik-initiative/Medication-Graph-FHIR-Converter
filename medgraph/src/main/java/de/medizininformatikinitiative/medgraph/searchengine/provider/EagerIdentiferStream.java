package de.medizininformatikinitiative.medgraph.searchengine.provider;

import java.util.List;
import java.util.stream.Stream;

/**
 * {@link IdentifierStream}-implementation which immediately processes the base stream and caches the results.
 * This has the advantage that if {@link #getIdentifiers()} is called multiple times, the previous transformations
 * only have to be calculated once. The disadvantage is
 *
 * @author Markus Budeus
 */
public class EagerIdentiferStream<S> implements IdentifierStream<S> {

	private final boolean isParallel;
	private final List<MappedIdentifier<S>> identifiers;

	public EagerIdentiferStream(IdentifierStream<S> base) {
		Stream<MappedIdentifier<S>> input = base.getIdentifiers();
		isParallel = input.isParallel();
		this.identifiers = base.getIdentifiers().toList();
	}

	@Override
	public Stream<MappedIdentifier<S>> getIdentifiers() {
		if (isParallel) {
			return identifiers.parallelStream();
		} else {
			return identifiers.stream();
		}
	}
}
