package de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class only to be used for testing. Transforms {@link Matchable}s based on a pre-defined map.
 *
 * @author Markus Budeus
 */
public class PredefinedMatchTransformer extends MatchTransformer {

	public static final String NAME = "PredefinedMatchTransformer";
	public static final String DESC = "Transforms based on a pre-defined map.";

	private final Map<Matchable, List<Matchable>> transformationMap;

	public PredefinedMatchTransformer(Map<Matchable, List<Matchable>> transformationMap) {
		this.transformationMap = transformationMap;
	}

	@Override
	protected List<Matchable> transformInternal(Matchable matchable, SearchQuery query) {
		List<Matchable> result = transformationMap.get(matchable);
		if (result == null) result = new ArrayList<>();
		return result;
	}

	@Override
	public String getDescription() {
		return DESC;
	}

	@Override
	public String toString() {
		return NAME;
	}
}
