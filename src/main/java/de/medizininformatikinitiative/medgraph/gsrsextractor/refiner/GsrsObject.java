package de.medizininformatikinitiative.medgraph.gsrsextractor.refiner;

import java.util.Arrays;

/**
 * Represents an objects from the Global Substance Registration System (GSRS) with some relevant data.
 *
 * @author Markus Budeus
 */
public class GsrsObject {

	public final String uuid;
	public final String name;
	public final String unii;
	public final String[] primaryRxcui;
	public final String[] alternativeRxcui;
	public final String primaryCas;
	public final String[] alternativeCas;
	public final String substanceClass;

	public final String status;

	public GsrsObject(String uuid, String name, String unii, String[] primaryRxcui, String[] alternativeRxcui,
	                  String primaryCas, String[] alternativeCas, String substanceClass, String status) {
		this.uuid = uuid;
		this.name = name;
		this.unii = unii;
		this.primaryRxcui = primaryRxcui;
		this.alternativeRxcui = alternativeRxcui;
		this.primaryCas = primaryCas;
		this.alternativeCas = alternativeCas;
		this.substanceClass = substanceClass;
		this.status = status;
	}

	@Override
	public String toString() {
		return "GsrsObject{" +
				"uuid='" + uuid + '\'' +
				", name='" + name + '\'' +
				", unii='" + unii + '\'' +
				", primaryRxcui=" + Arrays.toString(primaryRxcui) +
				", alternativeRxcui=" + Arrays.toString(alternativeRxcui) +
				", primaryCas='" + primaryCas + '\'' +
				", alternativeCas=" + Arrays.toString(alternativeCas) +
				", substanceClass='" + substanceClass + '\'' +
				", status='" + status + '\'' +
				'}';
	}
}
