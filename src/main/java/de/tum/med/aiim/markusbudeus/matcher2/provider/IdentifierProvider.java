package de.tum.med.aiim.markusbudeus.matcher2.provider;

import java.util.Iterator;
import java.util.List;

public interface IdentifierProvider<S> extends Iterable<MappedIdentifier<S>> {

	List<MappedIdentifier<S>> getIdentifiers();

	@Override
	default Iterator<MappedIdentifier<S>> iterator() {
		return getIdentifiers().iterator();
	}
}
