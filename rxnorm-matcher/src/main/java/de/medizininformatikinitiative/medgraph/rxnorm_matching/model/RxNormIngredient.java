package de.medizininformatikinitiative.medgraph.rxnorm_matching.model;

import java.util.List;

/**
 * An RxNorm IN/PIN
 *
 * @author Markus Budeus
 */
public class RxNormIngredient extends RxNormConcept {

	private static final List<RxNormTermType> ALLOWED_TYPES = List.of(RxNormTermType.IN, RxNormTermType.PIN);

	public RxNormIngredient(String rxcui, String name, RxNormTermType type) {
		super(rxcui, name, type);
		if (!ALLOWED_TYPES.contains(type)) {
			throw new IllegalArgumentException("Not a valid term type for an ingredient: "+type);
		}
	}

}
