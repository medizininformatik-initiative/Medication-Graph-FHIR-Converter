package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

/**
 * Represents where an {@link OriginalMatch} "came from".
 *
 * @author Markus Budeus
 */
public interface Source {

	/**
	 * Indicates the source of this match is unknown.
	 */
	Source UNKNOWN = new Source() {	};

}
