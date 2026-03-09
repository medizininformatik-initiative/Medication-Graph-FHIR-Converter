package de.medizininformatikinitiative.medgraph.rxnorm_matching.model;

import java.util.List;

/**
 * An RxNorm SCDC with all associated ingredients (all connected via has_ingredient or has_precise_ingredient).
 *
 * @author Markus Budeus
 */
public class RxNormSCDCWithIngredients extends RxNormSCDC {

	private final List<RxNormIngredient> ingredients;
	/**
	 * The canonical ingredient. This is the IN/PIN exactly referred to by this SCDC and to which the specified amount
	 * refers.
	 */
	private final RxNormIngredient canonicalIngredient;

	/**
	 * Builds a new {@link RxNormSCDCWithIngredients} and tries to find the canonical ingredient. Throws an
	 * {@link IllegalArgumentException} if no canonical ingredient can be found.
	 * @param rxcui The RXCUI of this SCDC.
	 * @param name The full name of this SCDC (e.g. "cetirizine hydrochloride 10 MG Oral Capsule")
	 * @param ingredients All ingredients assigned to this SCDC via has_ingredient or has_precise_ingredient
	 */
	public RxNormSCDCWithIngredients(String rxcui, String name, List<RxNormIngredient> ingredients) {
		super(rxcui, name);
		this.ingredients = ingredients;
		canonicalIngredient = findCanonicalIngredient(ingredients, super.getIngredientName());
	}

	public RxNormSCDCWithIngredients(RxNormSCDC base, List<RxNormIngredient> ingredients) {
		super(base);
		this.ingredients = ingredients;
		canonicalIngredient = findCanonicalIngredient(ingredients, super.getIngredientName());
	}

	public List<RxNormIngredient> getIngredients() {
		return ingredients;
	}

	private static RxNormIngredient findCanonicalIngredient(List<RxNormIngredient> candidates, String name) {
		List<RxNormIngredient> canonicals = candidates.stream().filter(i -> i.getName().equals(name)).toList();

		if (canonicals.isEmpty()) {
			throw new IllegalArgumentException("Could not find canonical ingredient! " +
					"Looking for name \"" + name + "\", but got " +
					candidates.stream().map(RxNormConcept::getName).toList());
		}
		if (canonicals.size() > 1) {
			throw new IllegalArgumentException("Could not find canonical ingredient! " +
					"Looking for name \"" + name + "\", but got multiple hits in list: " +
					candidates.stream().map(RxNormConcept::getName).toList());
		}
		return canonicals.getFirst();
	}

	public RxNormIngredient getCanonicalIngredient() {
		return canonicalIngredient;
	}
}

