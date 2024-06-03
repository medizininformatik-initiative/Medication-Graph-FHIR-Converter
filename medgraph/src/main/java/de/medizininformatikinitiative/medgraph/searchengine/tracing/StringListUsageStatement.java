package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Designates specific strings from a list of strings having been used.
 *
 * @author Markus Budeus
 * @see InputUsageStatement
 */
public class StringListUsageStatement extends AbstractUsageStatement<List<String>> {

	/**
	 * The indices of the strings within the input list which were used.
	 */
	@NotNull
	private final Set<Integer> usedIndices;

	/**
	 * Creates a new string list usage statement.
	 *
	 * @param original       the original list of strings from which some were used
	 * @param usedIndices the index within the original list of each string that was used
	 * @throws IllegalArgumentException if any element in the used indices is out of range for the original list
	 * @throws NullPointerException     either argument is null or any element from usedIndices is null
	 */
	public StringListUsageStatement(@NotNull List<String> original, @NotNull Set<Integer> usedIndices) {
		super(original);
		this.usedIndices = usedIndices;
		int inputSize = original.size();
		for (int usedIndex : usedIndices) {
			if (usedIndex < 0 || usedIndex >= inputSize) {
				throw new IllegalArgumentException(
						"The usedIndices list contains the element \"" + usedIndex +
								"\", which is out of bounds for the original list of size " + inputSize + "!");
			}
		}
	}

	@Override
	@NotNull
	public List<String> getUnusedParts() {
		List<String> unused = new ArrayList<>(getOriginal());
		for (int i : usedIndices) {
			unused.set(i, null);
		}
		unused.removeIf(Objects::isNull);
		return unused;
	}

	@Override
	@NotNull
	public List<String> getUsedParts() {
		List<String> used = new ArrayList<>(usedIndices.size());
		int inputSize = getOriginal().size();
		for (int i = 0; i < inputSize; i++) {
			if (usedIndices.contains(i)) used.add(getOriginal().get(i));
		}
		return used;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		if (!super.equals(object)) return false;
		StringListUsageStatement that = (StringListUsageStatement) object;
		return Objects.equals(usedIndices, that.usedIndices);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), usedIndices);
	}
}
