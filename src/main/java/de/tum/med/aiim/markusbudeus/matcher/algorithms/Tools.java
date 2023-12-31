package de.tum.med.aiim.markusbudeus.matcher.algorithms;

import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Tools {

	/**
	 * This is a last-resort sorting algorithm which sorts a set of identifier targets which all match equally well
	 * in a deterministic fashion.
	 */
	public static List<MatchingTarget> sortDeterministically(Set<MatchingTarget> target) {
		List<MatchingTarget> list = new ArrayList<>(target);
		Comparator<MatchingTarget> c1 = Comparator.comparing(MatchingTarget::getName);
		Comparator<MatchingTarget> c2 = c1.thenComparing(MatchingTarget::getMmiId);
		list.sort(c2);
		return list;
	}

}
