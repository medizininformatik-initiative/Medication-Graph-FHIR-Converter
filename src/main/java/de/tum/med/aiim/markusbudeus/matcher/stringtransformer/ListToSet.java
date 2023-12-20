package de.tum.med.aiim.markusbudeus.matcher.stringtransformer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListToSet implements Transformer<List<String>, Set<String>> {
	@Override
	public Set<String> transform(List<String> source) {
		return new HashSet<>(source);
	}
}
