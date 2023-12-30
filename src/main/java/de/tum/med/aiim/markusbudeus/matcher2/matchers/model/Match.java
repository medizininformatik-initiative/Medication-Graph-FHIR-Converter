package de.tum.med.aiim.markusbudeus.matcher2.matchers.model;

import de.tum.med.aiim.markusbudeus.matcher2.provider.MappedIdentifier;

import java.util.Set;

public interface Match<S> {

	Set<MappedIdentifier<S>> getBestMatches();

}
