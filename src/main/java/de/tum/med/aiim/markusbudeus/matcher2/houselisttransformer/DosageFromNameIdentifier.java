package de.tum.med.aiim.markusbudeus.matcher2.houselisttransformer;

import de.tum.med.aiim.markusbudeus.matcher2.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher2.tools.DosageDetector;

import java.util.List;
import java.util.stream.Collectors;

public class DosageFromNameIdentifier implements HouselistTransformer {

	/**
	 * Attempts to retrieve dosage information from the {@link HouselistEntry#name} and assign it to the corresponding
	 * fields. If {@link HouselistEntry#activeIngredientDosages} is not null for the passed entry, nothing is
	 * performed.
	 */
	@Override
	public void transform(HouselistEntry entry) {
		if (entry.activeIngredientDosages != null) return;

		List<DosageDetector.DetectedDosage> dosages = DosageDetector.detectDosages(entry.name);
		entry.activeIngredientDosages = dosages.stream().map(DosageDetector.DetectedDosage::getDosage).collect(
				Collectors.toList());
	}

}
