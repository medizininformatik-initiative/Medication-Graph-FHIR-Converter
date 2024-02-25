package de.medizininformatikinitiative.medgraph.matcher.matcher.model;

import de.medizininformatikinitiative.medgraph.matcher.provider.MappedIdentifier;

import java.util.Set;

/**
 * The result of running a {@link de.medizininformatikinitiative.medgraph.matcher.matcher.IMatcher} against a
 * search term.
 *
 * @param <S> the type of objects matched
 * @author Markus Budeus
 */
public interface Match<S> {

	Set<MappedIdentifier<S>> getBestMatches();

}
