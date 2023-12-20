package de.tum.med.aiim.markusbudeus.matcher.stringtransformer;

import java.util.List;

public class WhitespaceTokenizer implements Transformer<String, List<String>> {

	@Override
	public List<String> transform(String source) {
		List<String> list = new java.util.ArrayList<>(List.of(source.split(" ")));
		list.removeIf(String::isBlank);
		return list;
	}

}
