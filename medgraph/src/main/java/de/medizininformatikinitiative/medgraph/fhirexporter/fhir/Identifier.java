package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

import java.util.Objects;

/**
 * This class is an implementation of the "Basismodul Medikation (2023)"'s Identifier, which is part of some other objects.
 *
 * @author Markus Budeus
 */
public class Identifier {

	public static Identifier temporaryId(String id) {
		return new Identifier(
				Use.TEMP,
				null,
				null,
				id
		);
	}

	public static Identifier combinedMedicalProductSubproductIdentifier(long parentMmiId, int childNo, Long organizationMmiId) {
		return Identifier.temporaryId(rawStringIdFromProductAndOrganizationMmiId(parentMmiId, organizationMmiId) + "-" + childNo);
	}

	public static Identifier fromOrganizationMmiId(long mmiId) {
		return temporaryId(String.valueOf(mmiId));
	}

	public static Identifier fromSubstanceMmiId(long mmiId) {
		return temporaryId(String.valueOf(mmiId));
	}

	public static Identifier fromProductAndOrganizationMmiId(long productMmiId, Long organizationMmiId) {
		return temporaryId(rawStringIdFromProductAndOrganizationMmiId(productMmiId, organizationMmiId));
	}

	private static String rawStringIdFromProductAndOrganizationMmiId(long productMmiId, Long organizationMmiId) {
		String productId = String.valueOf(productMmiId);
		if (organizationMmiId != null) {
			return productId + "-" + organizationMmiId;
		} else {
			return productId;
		}
	}

	private String use;
	public CodeableConcept type;
	public String system;
	public String value;
	public String notice;
	// Period omitted
	// Assigner omitted

	public Identifier(Use use, CodeableConcept type, Uri system, String value) {
		this.use = use.toString();
		this.type = type;
		this.system = system != null ? system.value : null;
		this.value = value;
	}

	public String getUse() {
		return use;
	}

	public void setUse(Use use) {
		if (use == null) {
			this.use = null;
		} else {
			this.use = use.toString();
		}
	}

	public enum Use {
		USUAL,
		OFFICIAL,
		TEMP,
		SECONDARY,
		OLD;

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		Identifier that = (Identifier) object;
		return Objects.equals(use, that.use) && Objects.equals(type,
				that.type) && Objects.equals(system, that.system) && Objects.equals(value,
				that.value) && Objects.equals(notice, that.notice);
	}

	@Override
	public int hashCode() {
		return Objects.hash(system, value);
	}

	@Override
	public String toString() {
		return "Identifier{" +
				", system='" + system + '\'' +
				", value='" + value + '\'' +
				'}';
	}
}
