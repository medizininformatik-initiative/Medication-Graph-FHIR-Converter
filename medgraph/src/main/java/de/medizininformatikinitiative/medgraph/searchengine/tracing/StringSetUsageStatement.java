package de.medizininformatikinitiative.medgraph.searchengine.tracing;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Designates specific strings from a set of strings having been used.
 *
 * @author Markus Budeus
 * @see InputUsageStatement
 */
public class StringSetUsageStatement extends AbstractUsageStatement<Set<String>> {

	/**
	 * The tokens from the input set that were used.
	 */
	@NotNull
	private final Set<String> usedTokens;

	/**
	 * Creates a new string list usage statement.
	 *
	 * @param original   the original list of strings from which some were used
	 * @param usedTokens each string from the original list that was used
	 * @throws IllegalArgumentException if any element in usedTokens does not exist in the original
	 */
	public StringSetUsageStatement(@NotNull Set<String> original, @NotNull Set<String> usedTokens) {
		super(original);
		this.usedTokens = usedTokens;
		for (String token : usedTokens) {
			if (!original.contains(token)) throw new IllegalArgumentException(
					"The token \"" + token + "\" is given as part of usedTokens, but is not part of the original string set" +
							"(which is \"" + original + "\")!");
		}
	}

	@Override
	@NotNull
	public Set<String> getUnusedParts() {
		Set<String> unused = new HashSet<>(getOriginal());
		unused.removeIf(usedTokens::contains);
		return unused;
	}

	@Override
	@NotNull
	public Set<String> getUsedParts() {
		return Collections.unmodifiableSet(usedTokens);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		if (!super.equals(object)) return false;
		StringSetUsageStatement that = (StringSetUsageStatement) object;
		return Objects.equals(usedTokens, that.usedTokens);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), usedTokens);
	}
}
