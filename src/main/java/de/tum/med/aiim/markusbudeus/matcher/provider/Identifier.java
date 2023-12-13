package de.tum.med.aiim.markusbudeus.matcher.provider;

import java.util.ArrayList;
import java.util.List;

public class Identifier<S> {

	public final S identifier;
	public final List<IdentifierTarget> targets;

	public Identifier(S identifier) {
		this.identifier = identifier;
		this.targets = new ArrayList<>();
	}

	@Override
	public String toString() {
		List<String> names = targets.stream().map(t -> t.name).toList();
		return identifier.toString() + ": " + String.join("/", names.toArray(new String[0]));
	}
}
