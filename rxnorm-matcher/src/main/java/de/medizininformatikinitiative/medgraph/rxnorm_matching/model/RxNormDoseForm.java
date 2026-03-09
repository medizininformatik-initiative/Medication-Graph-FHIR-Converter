package de.medizininformatikinitiative.medgraph.rxnorm_matching.model;

/**
 * @author Markus Budeus
 */
public class RxNormDoseForm extends RxNormConcept {

	public RxNormDoseForm(String rxcui, String name) {
		super(rxcui, name, RxNormTermType.DF);
	}

}
