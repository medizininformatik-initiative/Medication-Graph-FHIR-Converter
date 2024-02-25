package de.medizininformatikinitiative.medgraph.matcher.provider;

import java.util.Iterator;
import java.util.List;

/**
 * Provides a set of {@link MappedIdentifier}s, which are a term of any type mapped to a
 * {@link de.medizininformatikinitiative.medgraph.matcher.model.MatchingTarget}.
 *
 * @param <S> the type of identifier term
 * @author Markus Budeus
 */
public interface IdentifierProvider<S> extends Iterable<MappedIdentifier<S>> {

	List<MappedIdentifier<S>> getIdentifiers();

	@Override
	default Iterator<MappedIdentifier<S>> iterator() {
		return getIdentifiers().iterator();
	}
}
