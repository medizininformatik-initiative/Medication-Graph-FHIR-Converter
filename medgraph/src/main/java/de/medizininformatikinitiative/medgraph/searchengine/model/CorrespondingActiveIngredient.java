package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	@NotNull
	private final String correspondingSubstanceName;
	/**
	 * The amount of the substance this ingredient corresponds to. (If the actual substance were 16.68mg of midazolam
	 * hydrochloride, this would be 15mg [of midazolam]).
	 */
	@Nullable
	private final AmountOrRange correspondingSubstanceAmount;

	public CorrespondingActiveIngredient(@NotNull String substanceName, @Nullable AmountOrRange amount,
	                                     @NotNull String correspondingSubstanceName,
	                                     @Nullable AmountOrRange correspondingSubstanceAmount) {
		super(substanceName, amount);
		this.correspondingSubstanceName = correspondingSubstanceName;
		this.correspondingSubstanceAmount = correspondingSubstanceAmount;
	}

	/**
	 * Return the name of the substance this ingredient corresponds to. (I.e. this would be "Midazolam", if the actual
	 * ingredient is "Midazolam hydrochloride").
	 */
	public @NotNull String getCorrespondingSubstanceName() {
		return correspondingSubstanceName;
	}

	/**
	 * Returns the amount of the substance this ingredient corresponds to. (If the actual substance were 16.68mg of
	 * midazolam hydrochloride, this would be 15mg [of midazolam]).
	 */
	public @Nullable AmountOrRange getCorrespondingSubstanceAmount() {
		return correspondingSubstanceAmount;
	}

	@Override
	public String toString() {
		return super.toString() + " (" + correspondingSubstanceAmount + " " + correspondingSubstanceName + ")";
	}
}
