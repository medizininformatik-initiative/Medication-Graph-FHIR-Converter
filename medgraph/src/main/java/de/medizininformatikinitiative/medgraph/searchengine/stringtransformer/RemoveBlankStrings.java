package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * A transformer which removes empty strings from a list.
 *
 * @author Markus Budeus
 */
public class RemoveBlankStrings implements Transformer<List<String>, List<String>> {

	@Override
	public List<String> apply(List<String> source) {
		ArrayList<String> result = new ArrayList<>(source);
		for (int i = 0; i < result.size(); i++) {
			String entry = result.get(i);
			if (entry == null || entry.isBlank()) {
				result.remove(i);
				i--;
			}
		}
		return result;
	}

}
