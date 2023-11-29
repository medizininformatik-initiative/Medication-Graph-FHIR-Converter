package de.tum.med.aiim.markusbudeus.gsrsextractor;

import java.util.Arrays;

public class GsrsObject {

	public final String unii;
	public final String primaryRxcui;
	public final String[] alternativeRxcui;

	public GsrsObject(String unii, String rxcui, String[] alternativeRxcui) {
		this.unii = unii;
		this.primaryRxcui = rxcui;
		this.alternativeRxcui = alternativeRxcui;
	}

	@Override
	public String toString() {
		return "GsrsInfo{" +
				"unii='" + unii + '\'' +
				", primaryRxcui='" + primaryRxcui + '\'' +
				", alternativeRxcui=" + Arrays.toString(alternativeRxcui) +
				'}';
	}
}
