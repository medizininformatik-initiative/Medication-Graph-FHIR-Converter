package de.medizininformatikinitiative.medgraph.tools.gsrsextractor.extractor;

/**
 * A GSRS search result with multiple matches.
 *
 * @author Markus Budeus
 */
public class GsrsMultiMatch extends GsrsSearchResult {

	public final String[] uuids;

	public GsrsMultiMatch(String cas, String[] uuids) {
		super(cas);
		this.uuids = uuids;
	}
}
