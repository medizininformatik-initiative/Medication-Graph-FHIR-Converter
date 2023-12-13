package de.tum.med.aiim.markusbudeus.matcher.transformer;

/**
 * A transformer is meant to transform synonyme names.
 */
public interface Transformer<S,T> {

	T transform(S source);

}
