package de.medizininformatikinitiative.medgraph.gsrsextractor.extractor;

import java.util.Arrays;

/**
 * The result of a CAS number GSRS search which matched only a single object.
 *
 * @author Markus Budeus
 */
public class GsrsSingleMatch extends GsrsSearchResult {

	public final String uuid;

	public final String name;
	public final String unii;
	public final String[] rxcui;

	public GsrsSingleMatch(String uuid, String name, String cas, String unii, String[] rxcui) {
		super(cas);
		this.uuid = uuid;
		this.name = name;
		this.unii = unii;
		this.rxcui = rxcui;
	}

	@Override
	public String toString() {
		return "GsrsInfo{" +
				"unii='" + unii + '\'' +
				", rxcui=" + Arrays.toString(rxcui) +
				'}';
	}
}
