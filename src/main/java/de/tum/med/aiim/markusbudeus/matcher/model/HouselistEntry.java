package de.tum.med.aiim.markusbudeus.matcher.model;

import de.tum.med.aiim.markusbudeus.matcher.model.Amount;
import de.tum.med.aiim.markusbudeus.matcher.model.Dosage;

import java.util.List;

/**
 * Represents a houselist entry which shall be matched. Each field may be null if unspecified in the house list.
 */
public class HouselistEntry {

	public String searchTerm;
	public String substanceName;
	public String productName;
	public String pzn;

	public List<Dosage> activeIngredientDosages;
	public Amount drugAmount;

}