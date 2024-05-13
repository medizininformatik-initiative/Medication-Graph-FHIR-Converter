package de.medizininformatikinitiative.medgraph.searchengine.model;

/**
 * Class which represents the search query information entered by the user.
 *
 * @author Markus Budeus
 */
public class SearchQuery {

	private final String productName;
	private final String substanceName;

	public SearchQuery(String productName, String substanceName) {
		this.productName = productName;
		this.substanceName = substanceName;
	}

	public String getProductName() {
		return productName;
	}

	public String getSubstanceName() {
		return substanceName;
	}

}
