package de.tum.med.aiim.markusbudeus.matcher2.stringtransformer;

/**
 * A transformer is meant to transform synonyme names.
 */
public interface Transformer<S,T> {

	Transformer<String, String> IDENTITY = new IdentityTransformer();

	T transform(S source);

	/**
	 * Creates a new transformer by chaining this instance with the given next transformer.
	 */
	default <V> Transformer<S,V> and(Transformer<T,V> next) {
		return new CompoundTransformer<>(this, next);
	}

}
