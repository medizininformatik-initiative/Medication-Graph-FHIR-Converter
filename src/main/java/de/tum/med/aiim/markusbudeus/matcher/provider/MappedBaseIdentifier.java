package de.tum.med.aiim.markusbudeus.matcher.provider;

public class MappedBaseIdentifier extends MappedIdentifier<String> {

	public MappedBaseIdentifier(String identifier) {
		this(new BaseIdentifier(identifier));
	}

	public MappedBaseIdentifier(BaseIdentifier identifier) {
		super(identifier);
	}

}
