package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.jetbrains.annotations.NotNull;

/**
 * States information about which part of the input to an operation (usually a search or something similar) turned out
 * to be relevant to finding a match. Here's an example:
 * <p>
 * We have a tool which detects specific substrings within a string (say, dosage terms within any generic string). This
 * tool, apart from reporting the detected dosages, also provides information about where in the source string these
 * dosages were detected. In other words, which parts of the source string were <b>used</b>. Therefore, as part of the
 * tool's output, a <code>InputUsageStatement&lt;String&gt;</code> is returned which exactly provides the information
 * about which part of the input was used.
 * <p>
 * Another example: We have a matcher which is given a list of search strings and another list of target strings to
 * compare against. The matcher is capable of matching some of the search strings to target strings and, as part
 * of its result, reports which of the search terms were relevant in matching to the target strings, via a
 * <code>InputUsageStatement&lt;Set&lt;String&gt;&gt;</code>.
 *
 * @param <T> the type of the input object for which the used parts are to be designated
 * @author Markus Budeus
 */
public interface InputUsageStatement<T> {

	// TODO Javadoc

	/**
	 * Returns the original input for which the used and unused parts are reported by this instance.
	 */
	@NotNull
	T getOriginal();

	/**
	 * Returns the unused parts of the original input.
	 */
	@NotNull
	T getUnusedParts();

	/**
	 * Returns the parts of the input which were used by whatever operation produced this statement.
	 */
	@NotNull
	T getUsedParts();

}
