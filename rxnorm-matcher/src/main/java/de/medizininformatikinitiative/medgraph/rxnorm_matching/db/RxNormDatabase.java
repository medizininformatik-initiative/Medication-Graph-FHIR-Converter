package de.medizininformatikinitiative.medgraph.rxnorm_matching.db;

import de.medizininformatikinitiative.medgraph.rxnorm_matching.model.DetailedRxNormSCD;

import java.util.Set;

/**
 * @author Markus Budeus
 */
public interface RxNormDatabase {

	/**
	 * Assumes the given ingredientRxCUIs are IN/PIN RxCUIs, resolves all connected SCDs and returns their RxCUIs.
	 */
	Set<String> getSCDRxCUIsForIngredientRxCUIs(Set<String> ingredientRxCUIs);

	/**
	 * Acquires additional information on the SCDs identified by the given RXCUIs. The information is returned as
	 * {@link DetailedRxNormSCD} objects.
	 */
	Set<DetailedRxNormSCD> resolveDetails(Set<String> scdRxCUIs);

}
