package de.medizininformatikinitiative.medgraph.rxnorm_matching.model;

/**
 * Any RxNorm Concept.
 *
 * @author Markus Budeus
 */
public abstract class RxNormConcept {
	private final String rxcui;
	private final String name;
	private final RxNormTermType type;

	public RxNormConcept(String rxcui, String name, RxNormTermType type) {
		this.rxcui = rxcui;
		this.name = name;
		this.type = type;
	}

	public String getRxcui() {
		return rxcui;
	}

	public String getName() {
		return name;
	}

	public RxNormTermType getType() {
		return type;
	}

	@Override
	public String toString() {
		return name;
	}
}
