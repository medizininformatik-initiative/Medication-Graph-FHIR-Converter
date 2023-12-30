package de.tum.med.aiim.markusbudeus.matcher.provider;

public class MappedBaseIdentifier extends MappedIdentifier<String> {

	public MappedBaseIdentifier(String identifier) {
		this(new BaseIdentifier(identifier));
	}

	public MappedBaseIdentifier(String identifier, IdentifierTarget target) {
		this(new BaseIdentifier(identifier), target);
	}

	public MappedBaseIdentifier(BaseIdentifier identifier) {
		super(identifier);
	}

	public MappedBaseIdentifier(BaseIdentifier identifier, IdentifierTarget target) {
		super(identifier);
		this.targets.add(target);
	}

}
