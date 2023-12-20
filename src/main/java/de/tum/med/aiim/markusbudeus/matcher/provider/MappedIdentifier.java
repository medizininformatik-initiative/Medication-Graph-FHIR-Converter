package de.tum.med.aiim.markusbudeus.matcher.provider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is an {@link Identifier} mapped to a set of identifier targets, which are whatever the corresponding identifier
 * describes.
 */
public class MappedIdentifier<S> {

	public final Identifier<S> identifier;
	public final Set<IdentifierTarget> targets;

	public MappedIdentifier(Identifier<S> identifier) {
		this.identifier = identifier;
		this.targets = new HashSet<>();
	}

	public MappedBaseIdentifier toBaseIdentifier() {
		MappedBaseIdentifier result = new MappedBaseIdentifier(identifier.getBaseIdentifier());
		result.targets.addAll(targets);
		return result;
	}

	@Override
	public String toString() {
		List<String> names = targets.stream().map(t -> t.name).toList();
		return identifier.toString() + ": " + String.join("/", names.toArray(new String[0]));
	}
}
