package de.medizininformatikinitiative.medgraph.searchengine.provider;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

/**
 * Provides a set of {@link MappedIdentifier}s, which are a term of any type mapped to a
 * {@link de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable}.
 *
 * @param <S> the type of identifier term
 * @author Markus Budeus
 */
public interface IdentifierProvider<S> extends Iterable<MappedIdentifier<S>> {

	List<MappedIdentifier<S>> getIdentifiers();

	@Override
	@NotNull
	default Iterator<MappedIdentifier<S>> iterator() {
		return getIdentifiers().iterator();
	}
}
