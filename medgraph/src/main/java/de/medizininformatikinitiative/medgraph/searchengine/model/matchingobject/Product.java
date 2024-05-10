package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

/**
 * A product which is an intermediate match of the search algorithm.
 *
 * @author Markus Budeus
 */
public class Product extends IdMatchable {

	public Product(long mmiId, String name) {
		super(mmiId, name);
	}

}
