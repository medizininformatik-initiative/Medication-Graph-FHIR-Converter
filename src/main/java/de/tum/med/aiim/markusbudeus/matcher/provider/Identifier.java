package de.tum.med.aiim.markusbudeus.matcher.provider;

/**
 * An Identifier is a "name" for something. It may be a transformed version of a {@link BaseIdentifier} or its a
 * {@link BaseIdentifier} itself.
 * @param <S> the type of the identifier
 */
public interface Identifier<S> {

	S getIdentifier();

	BaseIdentifier getBaseIdentifier();

}
