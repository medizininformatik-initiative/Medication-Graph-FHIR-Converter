package de.medizininformatikinitiative.medgraph.tools.edqmscraper;

/**
 * Represents a link from an EDQM Standard Terms concept to another concept.
 *
 * @author Markus Budeus
 */
public class Link {

	/**
	 * The code of the object this link refers to.
	 */
	private final String code;
	/**
	 * The name of the object this link refers to.
	 */
	private final String term;

	public Link(String code, String term) {
		this.code = code;
		this.term = term;
	}

	public String getCode() {
		return code;
	}

	public String getTerm() {
		return term;
	}
}
