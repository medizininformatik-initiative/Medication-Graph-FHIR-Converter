package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.tools.DosageDetector;
import org.jetbrains.annotations.NotNull;

/**
 * Indicates a dosage was found via the
 * {@link de.medizininformatikinitiative.medgraph.searchengine.tools.DosageDetector} scanning a string identifier.
 *
 * @author Markus Budeus
 */
public record DosageDetectorOrigin(
		@NotNull DosageDetector.DetectedDosage detectedDosage,
		@NotNull Identifier<String> identifier
) implements Origin {


	/**
	 * Returns the {@link DosageDetector.DetectedDosage}-instance which provided the corresponding dosage or amount.
	 */
	@Override
	@NotNull
	public DosageDetector.DetectedDosage detectedDosage() {
		return detectedDosage;
	}

	/**
	 * Returns the identifier string which was scanned in order to find this dosage.
	 */
	@Override
	@NotNull
	public Identifier<String> identifier() {
		return identifier;
	}

}
