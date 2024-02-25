package de.medizininformatikinitiative.medgraph.gsrsextractor.extractor;

/**
 * Represents the result of a GSRS search via CAS number.
 *
 * @author Markus Budeus
 */
public abstract class GsrsSearchResult {

	public final String cas;

	protected GsrsSearchResult(String cas) {
		this.cas = cas;
	}
}
