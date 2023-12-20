package de.tum.med.aiim.markusbudeus.matcher.resulttransformer;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;

import java.util.Set;

/**
 * Implementations transform identifier targets to new, different identifier targets. For example resolving a substance
 * target into a set of product targets.
 */
public interface ResultTransformer {

	Set<IdentifierTarget> transform(IdentifierTarget target, HouselistEntry entry);

}
