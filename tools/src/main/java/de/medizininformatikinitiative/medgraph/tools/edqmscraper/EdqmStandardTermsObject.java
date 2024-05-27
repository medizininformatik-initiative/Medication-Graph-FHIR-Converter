package de.medizininformatikinitiative.medgraph.tools.edqmscraper;

import com.google.gson.annotations.SerializedName;

/**
 * Represents an object of interest from the EDQM Standard Terms database.
 *
 * @author Markus Budeus
 */
public class EdqmStandardTermsObject {

	/**
	 * Class identifier for pharmaceutical dose forms, e.g. "granules for solution for injection"
	 */
	public static final String PHARMACEUTICAL_DOSE_FORM_CLASS = "PDF";
	/**
	 * Class identifier for intended sites of administration, e.g. "Oral", "Auricular"
	 */
	public static final String INTENDED_SITE_CLASS = "ISI";
	/**
	 * Class identifier for basic dose forms, e.g. "Tablet", "Solution".
	 */
	public static final String BASIC_DOSE_FORM_CLASS = "BDF";
	/**
	 * Class identifier for release characteristics, e.g. "prolonged-release".
	 */
	public static final String RELEASE_CHARACTERISTICS_CLASS = "RCA";

	/**
	 * The object class this object of interest belongs to.
	 */
	@SerializedName("class")
	private final String objectClass;

	/**
	 * This object's code within its object class.
	 */
	private final String code;

	/**
	 * The domain this object applies to. (Usually "Veterinary Only" or "Human and Veterinary")
	 */
	private final String domain;

	/**
	 * The status, e.g. "Current" or "Deprecated"
	 */
	private final String status;

	/**
	 * The name of this object.
	 */
	@SerializedName("english")
	private final String name;


	public EdqmStandardTermsObject(String objectClass, String code, String domain, String status, String name) {
		this.objectClass = objectClass;
		this.code = code;
		this.domain = domain;
		this.status = status;
		this.name = name;
	}

	public String getObjectClass() {
		return objectClass;
	}

	public String getCode() {
		return code;
	}

	public String getDomain() {
		return domain;
	}

	public String getStatus() {
		return status;
	}

	public String getName() {
		return name;
	}
}
