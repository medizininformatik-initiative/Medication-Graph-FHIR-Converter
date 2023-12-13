package de.tum.med.aiim.markusbudeus.matcher.provider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Identifier<S> {

	public final S identifier;
	public final Set<IdentifierTarget> targets;

	public Identifier(S identifier) {
		this.identifier = identifier;
		this.targets = new HashSet<>();
	}

	@Override
	public String toString() {
		List<String> names = targets.stream().map(t -> t.name).toList();
		return identifier.toString() + ": " + String.join("/", names.toArray(new String[0]));
	}
}
