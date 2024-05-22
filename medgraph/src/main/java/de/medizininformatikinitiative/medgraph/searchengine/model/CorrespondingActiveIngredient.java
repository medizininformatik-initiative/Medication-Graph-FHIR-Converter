package de.medizininformatikinitiative.medgraph.searchengine.model;

/**
 * An active ingredient which corresponds to a different one. For example, this may be 16.68mg of midazolam
 * hydrochloride which correspond to 15mg of midazolam.
 *
 * @author Markus Budeus
 */
public class CorrespondingActiveIngredient extends ActiveIngredient {

	/**
	 * The name of the substance this ingredient corresponds to. (I.e. this would be "Midazolam", if the actual
	 * ingredient is "Midazolam hydrochloride").
	 */
	private final String correspondingSubstanceName;
	/**
	 * The amount of the substance this ingredient corresponds to. (If the actual substance were 16.68mg of midazolam
	 * hydrochloride, this would be 15mg [of midazolam]).
	 */
	private final Amount correspondingSubstanceAmount;

	public CorrespondingActiveIngredient(String substanceName, Amount amount, String correspondingSubstanceName,
	                                     Amount correspondingSubstanceAmount) {
		super(substanceName, amount);
		this.correspondingSubstanceName = correspondingSubstanceName;
		this.correspondingSubstanceAmount = correspondingSubstanceAmount;
	}

	@Override
	public String toString() {
		return super.toString() + " (" + correspondingSubstanceAmount + " " + correspondingSubstanceName + ")";
	}
}
