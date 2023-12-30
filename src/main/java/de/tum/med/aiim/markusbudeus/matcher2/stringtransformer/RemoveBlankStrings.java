package de.tum.med.aiim.markusbudeus.matcher2.stringtransformer;

import java.util.ArrayList;
import java.util.List;

public class RemoveBlankStrings implements Transformer<List<String>, List<String>> {

	@Override
	public List<String> transform(List<String> source) {
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
