package de.tum.med.aiim.markusbudeus.fhirexporter.resource;

public class Identifier {

	public static Identifier fromMmiId(long mmiId) {
		return new Identifier(
				Use.TEMP,
				null,
				new Uri("https://www.mmi.de/mmi-pharmindex/mmi-pharmindex-daten"),
				String.valueOf(mmiId)
		);
	}

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
