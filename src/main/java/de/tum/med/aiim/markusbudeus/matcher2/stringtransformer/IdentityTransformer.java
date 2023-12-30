package de.tum.med.aiim.markusbudeus.matcher2.stringtransformer;

public class IdentityTransformer implements Transformer<String, String> {
	
	@Override
	public String transform(String source) {
		return source;
	}

}
