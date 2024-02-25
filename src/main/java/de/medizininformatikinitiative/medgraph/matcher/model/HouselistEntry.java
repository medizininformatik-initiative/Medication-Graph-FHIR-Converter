package de.medizininformatikinitiative.medgraph.matcher.model;

import java.util.List;

/**
 * Represents a houselist entry which shall be matched. Each field may be null if unspecified in the house list.
 *
 * @author Markus Budeus
 */
public class HouselistEntry {

	public String searchTerm;
	public String substanceName;
	public String productName;
	public String pzn;

	public List<Dosage> activeIngredientDosages;
	public Amount drugAmount;

}
