package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.InputUsageStatement;

import java.util.Objects;

/**
 * @author Markus Budeus
 */
public class Tools {

	/**
	 * Verifies the given usage statement's original value is the value that would be produced by the given transformer
	 * when provided with the given input. Throws an {@link IllegalArgumentException} if this is not the case.
	 */
	static <S, T, U extends InputUsageStatement<S>, V extends InputUsageStatement<T>> void ensureValidity(
			TraceableTransformer<S, T, U, V> transformer, S input, V usageStatement) {
		if (!Objects.equals(transformer.apply(input), (usageStatement.getOriginal()))) {
			throw new IllegalArgumentException("The given input, when transformed by this transformer," +
					" would not result in the original value of the given usage statement.");
		}
	}

}
