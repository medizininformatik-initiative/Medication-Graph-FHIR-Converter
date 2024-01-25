package de.tum.med.aiim.markusbudeus.matcher.model;

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
		return super.toString() + " ("+correspondingSubstanceAmount+" "+correspondingSubstanceName+")";
	}
}
