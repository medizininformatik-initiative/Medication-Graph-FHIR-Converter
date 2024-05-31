package de.medizininformatikinitiative.medgraph.searchengine.tracing;

/**
 * @author Markus Budeus
 */
public class IntRange {

	// TODO Javadoc
	private final int from;
	private final int to;

	public IntRange(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}
}
