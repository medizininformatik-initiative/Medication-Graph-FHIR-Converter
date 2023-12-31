package de.tum.med.aiim.markusbudeus.matcher.stringtransformer;

public class IdentityTransformer implements Transformer<String, String> {
	
	@Override
	public String transform(String source) {
		return source;
	}

}
