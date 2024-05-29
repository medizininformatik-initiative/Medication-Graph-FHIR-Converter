package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders.EdqmStandardTermsLoader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	private final List<EdqmConcept> characteristics;

	public EdqmPharmaceuticalDoseForm(@NotNull String code, @NotNull String name,
	                                  @NotNull List<EdqmConcept> characteristics) {
		super(code, name, "Pharmaceutical dose form");
		if (!code.startsWith(EdqmStandardTermsLoader.EDQM_PDF_CLASS)) {
			throw new IllegalArgumentException("Code for Pharmaceutical dose form concepts must start with \""
					+ EdqmStandardTermsLoader.EDQM_PDF_CLASS + "-\"!");
		}
		this.characteristics = Collections.unmodifiableList(new ArrayList<>(characteristics));
	}

	public @NotNull List<EdqmConcept> getCharacterristics() {
		return characteristics;
	}
}
