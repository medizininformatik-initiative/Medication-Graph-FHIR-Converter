package de.tum.med.aiim.markusbudeus.matcher;

public class HouselistEntry {

	public final String substanceName;
	public final String atc;
	public final String ask;
	public final String pzn;
	public final String noisySubstanceName;

	public HouselistEntry(String substanceName, String atc, String ask, String pzn, String noisySubstanceName) {
		this.substanceName = substanceName;
		this.atc = atc;
		this.ask = ask;
		this.pzn = pzn;
		this.noisySubstanceName = noisySubstanceName;
	}
}
