package de.tum.med.aiim.markusbudeus.fhirexporter.resource;

public class Identifier {

	private Code use;
	public CodeableConcept type;
	public Uri system;
	public String value;
	// Period omitted
	// Assigner omitted

	public Identifier(Use use, CodeableConcept type, Uri system, String value) {
		this.use = new Code(use.toString());
		this.type = type;
		this.system = system;
		this.value = value;
	}

	public Code getUse() {
		return use;
	}

	public void setUse(Use use) {
		if (use == null) {
			this.use = null;
		} else {
			this.use = new Code(use.toString());
		}
	}

	private static enum Use {
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
