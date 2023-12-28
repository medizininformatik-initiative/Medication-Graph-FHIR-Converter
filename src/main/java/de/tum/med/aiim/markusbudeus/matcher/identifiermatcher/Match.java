package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;

import java.util.Set;

public interface Match<S> {

	Set<MappedIdentifier<S>> getBestMatches();

}