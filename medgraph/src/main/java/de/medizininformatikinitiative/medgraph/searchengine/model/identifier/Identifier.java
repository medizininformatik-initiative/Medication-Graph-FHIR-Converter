package de.medizininformatikinitiative.medgraph.searchengine.model.identifier;

/**
 * This interface represents objects that identify (i.e. describe) something using any object. The most intuitive type
 * would be a string identifier, but nothing stops you from describing something using a list of strings.
 *
 * @param <T> the type of object used to describe something
 * @author Markus Budeus
 */
public interface Identifier<T> {

	/**
	 * Returns the object that describes whatever is identified by this instance.
	 */
	T getIdentifier();

}
