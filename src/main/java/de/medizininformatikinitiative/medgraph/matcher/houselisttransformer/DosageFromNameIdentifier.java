package de.medizininformatikinitiative.medgraph.matcher.houselisttransformer;

import de.medizininformatikinitiative.medgraph.matcher.tools.DosageDetector;
import de.medizininformatikinitiative.medgraph.matcher.model.HouselistEntry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads the name of the house list entry to find dosages within. If found, they are added to the dosages given
 * in the {@link HouselistEntry}.
 *
 * @author Markus Budeus
 */
public class DosageFromNameIdentifier implements HouselistTransformer {

	/**
	 * Attempts to retrieve dosage information from the {@link HouselistEntry#searchTerm} and assign it to the corresponding
	 * fields. If {@link HouselistEntry#activeIngredientDosages} is not null for the passed entry, nothing is
	 * performed.
	 */
	@Override
	public void transform(HouselistEntry entry) {
		if (entry.activeIngredientDosages != null) return;

		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(entry.searchTerm);
		entry.activeIngredientDosages = dosages.stream().map(DosageDetector.DetectedDosage::getDosage).collect(
				Collectors.toList());
	}

}
