package de.tum.med.aiim.markusbudeus.matcher.provider;

public class BaseIdentifier implements Identifier<String> {

	private final String identifier;

	public BaseIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public BaseIdentifier getBaseIdentifier() {
		return this;
	}
}
