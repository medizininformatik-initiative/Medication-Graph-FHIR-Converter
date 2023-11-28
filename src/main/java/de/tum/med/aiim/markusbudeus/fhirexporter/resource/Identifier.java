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

	public static Identifier combinedMedicalProductSubproductIdentifier(long parentMmiId, int childNo) {
		Identifier identifier = Identifier.temporaryId(parentMmiId + "-" + childNo);
//		identifier.notice = "This is a temporary id assigned to a Medication object so it can be referenced by " +
//				"another Medication object (which is a combined preparation) as its ingredient. " +
//				"It is advised that after you uploaded all instances to the FHIR server, you remove this identifier " +
//				"and replace all references to it by references to the object's URI on the server.";
		return identifier;
	}

	public static Identifier fromOrganizationMmiId(long mmiId) {
		Identifier identifier = temporaryId(String.valueOf(mmiId));
//		identifier.notice = "This is a temporary id assigned to an Organization object so it can be referenced by " +
//				"Medication objects as manufacturer. " +
//				"It is advised that after you uploaded all instances to the FHIR server, you remove this identifier " +
//				"and replace all references to it by references to the object's URI on the server.";
		return identifier;
	}

	public static Identifier fromSubstanceMmiId(long mmiId) {
		Identifier identifier = temporaryId(String.valueOf(mmiId));
//		identifier.notice = "This is a temporary id assigned to a Substance object so it can be referenced by " +
//				"Medication objects as ingredient. " +
//				"It is advised that after you uploaded all instances to the FHIR server, you remove this identifier " +
//				"and replace all references to it by references to the object's URI on the server.";
		return identifier;
	}

	public static Identifier fromProductMmiId(long mmiId) {
		Identifier identifier = temporaryId(String.valueOf(mmiId));
//		identifier.notice = "This is a temporary id assigned to a Medication object. " +
//				"It contains the id of the corresponding product in the MMI PharmIndex PRODUCTS.CSV. You may remove " +
//				"this identifier.";
		return identifier;
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
