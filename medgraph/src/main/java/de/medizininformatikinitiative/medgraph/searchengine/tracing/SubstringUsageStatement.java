package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A usage statement for a string which indicates one or more substrings of the original string having been used.
 *
 * @author Markus Budeus
 */
public abstract class SubstringUsageStatement extends AbstractUsageStatement<String> {


	public SubstringUsageStatement(@NotNull String original) {
		super(original);
	}

	/**
	 * Returns the used ranges of the given string.
	 */
	public abstract Set<IntRange> getUsedRanges();

}
