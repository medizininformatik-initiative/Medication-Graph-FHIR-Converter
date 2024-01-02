package de.tum.med.aiim.markusbudeus.matcher.sample.synthetic;

import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;

public class SyntheticHouselistEntry extends HouselistEntry {

	public final String substanceName;
	public final String atc;
	public final String ask;
	public final String pzn;
	public final String noisySubstanceName;

	public SyntheticHouselistEntry(String substanceName, String atc, String ask, String pzn, String noisySubstanceName) {
		this.substanceName = substanceName;
		this.atc = atc;
		this.ask = ask;
		this.pzn = pzn;
		this.noisySubstanceName = noisySubstanceName;
		super.searchTerm = noisySubstanceName;
	}
}
