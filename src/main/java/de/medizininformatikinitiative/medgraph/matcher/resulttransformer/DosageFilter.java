package de.medizininformatikinitiative.medgraph.matcher.resulttransformer;

import de.medizininformatikinitiative.medgraph.matcher.model.MatchingTarget;
import de.medizininformatikinitiative.medgraph.matcher.model.HouselistEntry;
import de.medizininformatikinitiative.medgraph.matcher.judge.DosageMatchJudge;
import org.neo4j.driver.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Filter which removes product matches that completely mismatch dosage information given in the house list.
 * If the house list entry contains no dosage information, everything passes the filter.
 *
 * @author Markus Budeus
 */
public class DosageFilter implements Filter {

	private final DosageMatchJudge dosageMatchJudge;

	public DosageFilter(Session session) {
		this.dosageMatchJudge = new DosageMatchJudge(session);
	}

	@Override
	public List<Boolean> batchPassesFilter(List<MatchingTarget> targets, HouselistEntry entry) {
		List<Boolean> results = dosageMatchJudge.batchJudge(targets, entry).stream()
		                                        .map(d -> d >= DosageMatchJudge.MIN_SCORE_ON_MATCH)
		                                        .collect(Collectors.toList());
		for (int i = 0; i < targets.size(); i++) {
			if (targets.get(i).getType() != MatchingTarget.Type.PRODUCT) {
				results.set(i, true);
			}
		}
		return results;
	}

	@Override
	public boolean passesFilter(MatchingTarget target, HouselistEntry entry) {
		return target.getType() != MatchingTarget.Type.PRODUCT || dosageMatchJudge.judge(target,
				entry) >= DosageMatchJudge.MIN_SCORE_ON_MATCH;
	}

}
