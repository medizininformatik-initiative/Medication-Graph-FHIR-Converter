package de.medizininformatikinitiative.medgraph.matcher.stringtransformer;

/**
 * This transformer makes the names lowercase.
 *
 * @author Markus Budeus
 */
public class ToLowerCase implements Transformer<String, String> {

	@Override
	public String transform(String source) {
		return source.toLowerCase();
	}

}
