package de.tum.med.aiim.markusbudeus.matcher.stringtransformer;

/**
 * This transformer makes the names lowercase.
 */
public class ToLowerCase implements Transformer<String, String> {

	@Override
	public String transform(String source) {
		return source.toLowerCase();
	}

}
