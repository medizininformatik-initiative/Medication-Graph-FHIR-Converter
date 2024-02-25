package de.medizininformatikinitiative.medgraph.matcher.model;

/**
 * An active ingredient which corresponds to a different one. For example, this may be 16.68mg of midazolam
 * hydrochloride which correspond to 15mg of midazolam.
 *
 * @author Markus Budeus
 */
public class CorrespondingActiveIngredient extends ActiveIngredient {

	private final String correspondingSubstanceName;
	private final AmountRange correspondingSubstanceAmount;

	public CorrespondingActiveIngredient(String substanceName, AmountRange amount, String correspondingSubstanceName,
	                                     AmountRange correspondingSubstanceAmount) {
		super(substanceName, amount);
		this.correspondingSubstanceName = correspondingSubstanceName;
		this.correspondingSubstanceAmount = correspondingSubstanceAmount;
	}

	@Override
	public String toString() {
		return super.toString() + " (" + correspondingSubstanceAmount + " " + correspondingSubstanceName + ")";
	}
}
