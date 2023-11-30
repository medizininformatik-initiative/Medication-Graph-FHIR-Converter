package de.tum.med.aiim.markusbudeus.gsrsextractor.extractor;

public abstract class GsrsSearchResult {

	public final String cas;

	protected GsrsSearchResult(String cas) {
		this.cas = cas;
	}
}
