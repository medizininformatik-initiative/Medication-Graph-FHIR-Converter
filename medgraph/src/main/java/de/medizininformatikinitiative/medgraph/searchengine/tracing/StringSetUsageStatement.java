package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Markus Budeus
 */
public class StringSetUsageStatement extends AbstractUsageStatement<Set<String>> {

	// TODO Javadoc
	// TODO Test

	private final Set<String> usedTokens;

	public StringSetUsageStatement(Set<String> input, Set<String> usedTokens) {
		super(input);
		this.usedTokens = usedTokens;
		// TODO Verify the tokens are valid for the input!
	}

	@Override
	public Set<String> getUnusedParts() {
		Set<String> unused = new HashSet<>(getInput());
		unused.removeIf(usedTokens::contains);
		return unused;
	}

	@Override
	public Set<String> getUsedParts() {
		return Collections.unmodifiableSet(usedTokens);
	}
}
