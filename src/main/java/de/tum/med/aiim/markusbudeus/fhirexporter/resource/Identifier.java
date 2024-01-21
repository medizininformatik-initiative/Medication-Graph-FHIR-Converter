package de.tum.med.aiim.markusbudeus.fhirexporter.resource;

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
			return productId + "-" + String.valueOf(organizationMmiId);
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

}
