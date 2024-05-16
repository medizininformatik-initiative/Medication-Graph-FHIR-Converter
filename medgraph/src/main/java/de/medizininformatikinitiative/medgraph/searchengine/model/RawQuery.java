package de.medizininformatikinitiative.medgraph.searchengine.model;

/**
 * Raw Search query terms provided by the user.
 *
 * @author Markus Budeus
 */
public class RawQuery {

	/**
	 * Raw, unspecific query text provided by the user.
	 */
	public String query;
	/**
	 * Query text for product names.
	 */
	public String product;
	/**
	 * Query text for substance (i.e. active ingredient) names.
	 */
	public String substance;

}
