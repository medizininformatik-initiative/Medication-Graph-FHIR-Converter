package de.tum.med.aiim.markusbudeus.gsrsextractor;

import java.util.Arrays;

public class GsrsObject extends GsrsSearchResult {

	public final String uuid;

	public final String name;
	public final String unii;
	public final String[] rxcui;

	public GsrsObject(String uuid, String name, String cas, String unii, String[] rxcui) {
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
