package de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep;

import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.FilteringInfo;

/**
 * @author Markus Budeus
 */
public class FilteringStep implements Judgement {

	private final String name;
	private final String desc;
	private final FilteringInfo filteringInfo;

	public FilteringStep(String name, String desc, FilteringInfo filteringInfo) {
		this.name = name;
		this.desc = desc;
		this.filteringInfo = filteringInfo;
	}

	@Override
	public boolean passed() {
		return filteringInfo.passed();
	}

	@Override
	public FilteringInfo getJudgementInfo() {
		return filteringInfo;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String description() {
		return desc;
	}
}
