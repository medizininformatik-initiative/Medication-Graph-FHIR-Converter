package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.Identifier;

import java.util.Set;

public interface Match<S> {

	Set<Identifier<S>> getBestMatches();

}
