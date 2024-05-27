package de.medizininformatikinitiative.medgraph.tools.edqmscraper;

import java.util.List;

/**
 * Extension of {@link EdqmStandardTermsObject} which represents a pharmaceutical dose form.
 *
 * @author Markus Budeus
 */
public class EdqmPharmaceuticalDoseForm extends EdqmStandardTermsObject {

	/**
	 * The codes of the basic dose form this standard term belongs to.
	 */
	private final List<String> basicDoseFormCodes;
	/**
	 * The codes of the intended site of application of this dose form.
	 */
	private final List<String> intendedSiteCodes;
	/**
	 * The codes of the release characteristics that apply to this dose form.
	 */
	private final List<String> releaseCharacteristicsCodes;

	public EdqmPharmaceuticalDoseForm(EdqmStandardTermsObject baseObject, List<String> basicDoseFormCodes,
	                                  List<String> intendedSiteCodes, List<String> releaseCharacteristicsCodes) {
		super(baseObject.getObjectClass(), baseObject.getCode(), baseObject.getDomain(), baseObject.getStatus(),
				baseObject.getName());
		if (!PHARMACEUTICAL_DOSE_FORM_CLASS.equals(baseObject.getObjectClass())) {
			throw new IllegalArgumentException(
					"Cannot generate an EdqmPharmaceuticalDoseForm from an object that is not one!");
		}
		this.basicDoseFormCodes = basicDoseFormCodes;
		this.intendedSiteCodes = intendedSiteCodes;
		this.releaseCharacteristicsCodes = releaseCharacteristicsCodes;
	}

	public List<String> getBasicDoseFormCodes() {
		return basicDoseFormCodes;
	}

	public List<String> getIntendedSiteCodes() {
		return intendedSiteCodes;
	}

	public List<String> getReleaseCharacteristicsCodes() {
		return releaseCharacteristicsCodes;
	}
}
