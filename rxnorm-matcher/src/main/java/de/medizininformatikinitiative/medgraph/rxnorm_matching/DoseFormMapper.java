package de.medizininformatikinitiative.medgraph.rxnorm_matching;

import org.jetbrains.annotations.Nullable;

/**
 * @author Markus Budeus
 */
public interface DoseFormMapper {

	/**
	 * Returns the RxNorm dose form name for the dose form equivalent of the EDQM Standard Term identified by the given
	 * name.
	 */
	@Nullable
	String getRxNormDoseForm(String edqmDoseFormName);

}
