package de.tum.med.aiim.markusbudeus.matcher.houselisttransformer;

import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.tools.DosageDetector;

import java.util.List;
import java.util.stream.Collectors;

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
