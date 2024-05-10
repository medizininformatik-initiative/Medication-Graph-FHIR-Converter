package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

/**
 * A substance which is an intermediate match of the search algorithm.
 *
 * @author Markus Budeus
 */
public class Substance extends IdMatchable {

	public Substance(long mmiId, String name) {
		super(mmiId, name);
	}

}
