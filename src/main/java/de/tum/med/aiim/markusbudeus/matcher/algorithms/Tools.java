package de.tum.med.aiim.markusbudeus.matcher.algorithms;

import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Tools {

	/**
	 * This is a last-resort sorting algorithm which sorts a set of identifier targets which all match equally well
	 * in a deterministic fashion.
	 */
	public static List<IdentifierTarget> sortDeterministically(Set<IdentifierTarget> target) {
		List<IdentifierTarget> list = new ArrayList<>(target);
		Comparator<IdentifierTarget> c1 = Comparator.comparing(target1 -> target1.name);
		Comparator<IdentifierTarget> c2 = c1.thenComparing(target1 -> target1.mmiId);
		list.sort(c2);
		return list;
	}

}
