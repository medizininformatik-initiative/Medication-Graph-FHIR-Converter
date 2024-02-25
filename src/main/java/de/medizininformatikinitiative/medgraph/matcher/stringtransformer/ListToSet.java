package de.medizininformatikinitiative.medgraph.matcher.stringtransformer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A transformer which turns a list of strings into a set, thereby eliminating order information and duplicates.
 *
 * @author Markus Budeus
 */
public class ListToSet implements Transformer<List<String>, Set<String>> {
	@Override
	public Set<String> transform(List<String> source) {
		return new HashSet<>(source);
	}
}
