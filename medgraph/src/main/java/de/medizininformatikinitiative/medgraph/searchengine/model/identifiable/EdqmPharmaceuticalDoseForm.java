package de.medizininformatikinitiative.medgraph.searchengine.model.identifiable;

import de.medizininformatikinitiative.medgraph.common.EDQM;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents a EDQM pharmaceutical dose form.
 *
 * @author Markus Budeus
 */
public class EdqmPharmaceuticalDoseForm extends EdqmConcept {

	/**
	 * The characteristics (i.e. basic dose form, intended site, etc.) of this pharmaceutical dose form.
	 */
	@NotNull
	private final Set<EdqmConcept> characteristics;

	public EdqmPharmaceuticalDoseForm(@NotNull String code, @NotNull String name,
	                                  @NotNull Collection<EdqmConcept> characteristics) {
		super(code, name, EDQM.PHARMACEUTICAL_DOSE_FORM);
		this.characteristics = Set.copyOf(characteristics);
	}

	public @NotNull Set<EdqmConcept> getCharacteristics() {
		return characteristics;
	}
}
