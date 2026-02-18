package de.medizininfomatikinitiative.medgraph.rxmatching;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an RxNorm IN or PIN concept.
 *
 * @author Markus Budeus
 */
public record RxNormIngredient(String rxcui, String termType, String name) {

	@Override
	public @NotNull String toString() {
		return name;
	}
}