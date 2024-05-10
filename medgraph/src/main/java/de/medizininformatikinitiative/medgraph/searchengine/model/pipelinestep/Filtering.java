package de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep;

import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;

/**
 * A the result of a filtering step which filters out some {@link Matchable}s.
 *
 * @author Markus Budeus
 */
public class Filtering implements Judgement {

	private final String name;
	private final String description;
	private final boolean passed;

	public Filtering(String name, String description, boolean passed) {
		this.name = name;
		this.description = description;
		this.passed = passed;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public boolean isPassed() {
		return passed;
	}
}
