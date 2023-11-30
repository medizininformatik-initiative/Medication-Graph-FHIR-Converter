package de.tum.med.aiim.markusbudeus.gsrsextractor;

public class GsrsMultiMatch extends GsrsSearchResult {

	public final String[] uuids;

	public GsrsMultiMatch(String cas, String[] uuids) {
		super(cas);
		this.uuids = uuids;
	}
}
