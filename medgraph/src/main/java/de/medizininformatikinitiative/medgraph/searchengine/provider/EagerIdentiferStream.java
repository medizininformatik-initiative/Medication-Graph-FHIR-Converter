package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable;

import java.util.List;
import java.util.stream.Stream;

/**
 * {@link MappedIdentifierStream}-implementation which immediately processes the base stream and caches the results.
 * This has the advantage that if {@link #getIdentifiers()} is called multiple times, the previous transformations
 * only have to be calculated once. The disadvantage is
 *
 * @author Markus Budeus
 */
public class EagerIdentiferStream<S, T extends Identifiable> implements MappedIdentifierStream<S, T> {

	private final boolean isParallel;
	private final List<MappedIdentifier<S, T>> identifiers;

	public EagerIdentiferStream(MappedIdentifierStream<S, T> base) {
		Stream<MappedIdentifier<S, T>> input = base.getIdentifiers();
		isParallel = input.isParallel();
		this.identifiers = base.getIdentifiers().toList();
	}

	@Override
	public Stream<MappedIdentifier<S, T>> getIdentifiers() {
		if (isParallel) {
			return identifiers.parallelStream();
		} else {
			return identifiers.stream();
		}
	}
}
