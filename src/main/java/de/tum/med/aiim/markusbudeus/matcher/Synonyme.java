package de.tum.med.aiim.markusbudeus.matcher;

import java.util.ArrayList;
import java.util.List;

public class Synonyme {

	public final String synonyme;
	public final List<SynonymeTarget> targets;

	public Synonyme(String synonyme) {
		this.synonyme = synonyme;
		this.targets = new ArrayList<>();
	}
}
