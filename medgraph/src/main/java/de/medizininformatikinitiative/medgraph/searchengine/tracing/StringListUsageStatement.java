package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Markus Budeus
 */
public class StringListUsageStatement extends AbstractUsageStatement<List<String>> {

	// TODO Javadoc
	// TODO Test

	private final Set<Integer> usedIndices;

	public StringListUsageStatement(List<String> input, Set<Integer> usedIndices) {
		super(input);
		this.usedIndices = usedIndices;
		// TODO Verify the indices are valid for the input!
	}

	@Override
	public List<String> getUnusedParts() {
		List<String> unused = new ArrayList<>(getInput());
		for (int i : usedIndices) {
			unused.set(i, null);
		}
		unused.removeIf(Objects::isNull);
		return unused;
	}

	@Override
	public List<String> getUsedParts() {
		List<String> used = new ArrayList<>(usedIndices.size());
		for (int i : usedIndices) {
			used.add(getInput().get(i));
		}
		return used;
	}
}
