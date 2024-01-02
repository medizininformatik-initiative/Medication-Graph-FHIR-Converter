package de.tum.med.aiim.markusbudeus.matcher.resulttransformer;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.Amount;
import de.tum.med.aiim.markusbudeus.matcher.model.Dosage;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.resultranker.DosageMatchJudge;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

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
