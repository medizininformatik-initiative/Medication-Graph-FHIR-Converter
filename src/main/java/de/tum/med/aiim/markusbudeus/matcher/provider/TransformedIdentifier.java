package de.tum.med.aiim.markusbudeus.matcher.provider;

public class TransformedIdentifier<S> implements Identifier<S> {

	private final S identifier;
	private final Identifier<?> precursorIdentifier;

	public TransformedIdentifier(S identifier, Identifier<?> precursorIdentifier) {
		this.identifier = identifier;
		this.precursorIdentifier = precursorIdentifier;
	}

	@Override
	public S getIdentifier() {
		return identifier;
	}

	public Identifier<?> getPrecursorIdentifier() {
		return precursorIdentifier;
	}

	@Override
	public BaseIdentifier getBaseIdentifier() {
		return precursorIdentifier.getBaseIdentifier();
	}
}
