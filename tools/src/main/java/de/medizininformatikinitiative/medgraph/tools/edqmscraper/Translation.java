package de.medizininformatikinitiative.medgraph.tools.edqmscraper;

/**
 * @author Markus Budeus
 */
public class Translation {

	private final String language;
	private final String term;

	public Translation(String language, String term) {
		this.language = language;
		this.term = term;
	}

	public String getLanguage() {
		return language;
	}

	public String getTerm() {
		return term;
	}
}
