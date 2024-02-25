package de.medizininformatikinitiative.medgraph.matcher.model;

/**
 * A substance which is an intermediate match of the search algorithm.
 *
 * @author Markus Budeus
 */
public class Substance extends MatchingTarget {

	public Substance(long mmiId, String name) {
		super(mmiId, name);
	}

	@Override
	public Type getType() {
		return Type.SUBSTANCE;
	}
}
