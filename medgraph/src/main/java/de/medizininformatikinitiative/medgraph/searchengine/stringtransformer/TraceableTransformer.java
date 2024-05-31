package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.InputUsageStatement;

/**
 * In general (don't read, hard to understand - read the example below that): This is an extended {@link Transformer}
 * which attempts to keep {@link InputUsageStatement}s produced by whatever class consumes the output of this
 * transformer usable by providing a function {@link #reverseTransformUsageStatement(Object, InputUsageStatement)} which
 * transforms an {@link InputUsageStatement} to a corresponding instance for the transformer's input.
 * <p>
 * Example: This transformer tokenizes a string along whitespaces (and is therefore a
 * <code>Transformer&lt;String,Set&lt;String&gt;&gt;</code>) and the tokens are subsequently fed into a class which
 * uses them for matching. This class provides a <code>InputUsageStatement&lt;List&lt;String&gt;&gt;</code> as part of
 * the matching result, which contains information about which tokens were relevant for a matching result. Now, it would
 * be nice to know which parts of the original string correspond to these tokens, which would allow us to construct a
 * <code>InputUsageStatement&lt;String&gt;</code> for this original string. This can be achieved by using
 * {@link #reverseTransformUsageStatement(Object, InputUsageStatement)} and passing the original string and the
 * {@link InputUsageStatement} provided by the matcher class.
 * <p>
 * To make this a little more clear: Say the input was "May the force be with you". It was tokenized by this transformer
 * into ["May", "the", "force", "be", "with", "you"] and subsequently processed by a matcher, which reported that the
 * tokens "May" and "force" were relevant to the match found. When
 * {@link #reverseTransformUsageStatement(Object, InputUsageStatement) reverse transforming the usage statement}, the
 * class would generate a {@link InputUsageStatement statement} reporting that the substrings "May " and "force "
 * from the original input were used. (Possibly without the spaces or the spaces placed differently, this is up to the
 * implementing class. There is not always a single "correct" way to transform a usage statement, thus this may come
 * with a loss of information.)
 * <p>
 * The reason why this class features separate generics for the subclasses of the {@link InputUsageStatement} being used
 * is that this allows implementing classes to limit the support of back-transforming such instances to specific
 * implementations.
 *
 * @author Markus Budeus
 */
public interface TraceableTransformer<S, T, U extends InputUsageStatement<S>, V extends InputUsageStatement<T>>
		extends Transformer<S, T> {

	/**
	 * Transforms the given usageStatement, which reflects how the <i>output</i> of this transformer was used, to a
	 * different usage statement, which reflects how the <i>input</i> of this transformer was used. Note this may come
	 * with a slight loss of information, depending on the nature of this transformer.
	 *
	 * @param input          the input to this transformer which resulted in the given usage statement
	 * @param usageStatement the usage statement of the transformer's output
	 * @return a {@link InputUsageStatement} of the type supported by this instance which reflects which parts of the
	 * input to this transformer were used to match the given usage statement of the transformer's output
	 * @throws IllegalArgumentException if the given input, when transformed by this transformer, would not result in
	 *                                  the original input reported by the usageStatement
	 */
	U reverseTransformUsageStatement(S input, V usageStatement);

	/**
	 * Like {@link ##and(Transformer)}, but keeps the traceability. Obviously requires the chained transformer to
	 * produce a {@link InputUsageStatement} compatible to this instance.
	 *
	 * @param transformer the transformer to chain with
	 * @param <A>         the type of value produced by the given transformer
	 * @param <B>         the type of {@link InputUsageStatement} produced by the given transformer
	 * @return a compound transformer of this one and the given one
	 */
	default <A, B extends InputUsageStatement<A>> TraceableTransformer<S, A, U, B> andTraceable(
			TraceableTransformer<T, A, V, B> transformer) {
		return new TraceableCompoundTransformer<>(this, transformer);
	}

}
