package de.medizininformatikinitiative.medgraph.matcher.stringtransformer;

import de.medizininformatikinitiative.medgraph.matcher.tools.DosageDetector;

import java.util.List;

/**
 * A transformer which searches names for dosage information and clears it.
 *
 * @author Markus Budeus
 */
public class RemoveDosageInformation implements Transformer<String, String> {

	@Override
	public String transform(String source) {
		List<DosageDetector.DetectedDosage> detectedDosageList = DosageDetector.detectDosages(source);

		if (detectedDosageList.isEmpty()) return source;

		StringBuilder sb = new StringBuilder(source);
		for (int i = detectedDosageList.size() - 1; i >= 0; i--) {
			DosageDetector.DetectedDosage dosage = detectedDosageList.get(i);
			int start = dosage.getStartIndex();
			sb.delete(start, start + dosage.getLength());
		}

		String result = sb.toString();

		sb.setLength(0);

		return result;
	}

}
