package de.medizininformatikinitiative.medgraph.matcher.stringtransformer;

import java.util.List;

/**
 * A transformer which splits a string along its whitespaces.
 *
 * @author Markus Budeus
 */
public class WhitespaceTokenizer implements Transformer<String, List<String>> {

	@Override
	public List<String> transform(String source) {
		List<String> list = new java.util.ArrayList<>(List.of(source.split(" ")));
		list.removeIf(String::isBlank);
		return list;
	}

}