package de.tum.med.aiim.markusbudeus.matcher.stringtransformer;

import de.tum.med.aiim.markusbudeus.matcher.tools.DosageDetector;

import java.util.List;

/**
 * A transformer which searches names for dosage information and clears it.
 */
public class RemoveDosageInformation implements Transformer<String, String> {

	private final StringBuilder sb = new StringBuilder();

	@Override
	public String transform(String source) {
		List<DosageDetector.DetectedDosage> detectedDosageList = DosageDetector.detectDosages(source);

		if (detectedDosageList.isEmpty()) return source;

		sb.append(source);
		for (int i = detectedDosageList.size() - 1; i >= 0; i--) {
			DosageDetector.DetectedDosage dosage = detectedDosageList.get(i);
			sb.delete(dosage.getStartIndex(), dosage.getStartIndex() + dosage.getLength());
		}

		String result = sb.toString();

		sb.setLength(0);

		return result;
	}

}
