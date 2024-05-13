package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

/**
 * A {@link Transformer} which does nothing.
 *
 * @author Markus Budeus
 */
public class IdentityTransformer implements Transformer<String, String> {
	
	@Override
	public String transform(String source) {
		return source;
	}

}
