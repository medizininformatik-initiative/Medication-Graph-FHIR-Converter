package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * A transformer which splits a string along its whitespaces.
 *
 * @author Markus Budeus
 */
public class WhitespaceTokenizer implements Transformer<String, List<String>> {

	private final boolean considerQuotes;

	public WhitespaceTokenizer() {
		this(true);
	}

	/**
	 * Creates a new {@link WhitespaceTokenizer}.
	 * @param considerQuotes if true, multiple words within double quotes '"' are not separated.
	 */
	public WhitespaceTokenizer(boolean considerQuotes) {
		this.considerQuotes = considerQuotes;
	}

	@Override
	public List<String> apply(String source) {
		List<String> list;
		if (!considerQuotes) {
			list = simpleSplit(source);
		} else {
			list = splitWithQuotes(source);
		}
		list.removeIf(String::isBlank);
		return list;
	}

	private List<String> simpleSplit(String source) {
		return new java.util.ArrayList<>(List.of(source.split(" ")));
	}

	private List<String> splitWithQuotes(String source) {
		StringBuilder builder = new StringBuilder(24);
		List<String> parts = new ArrayList<>();
		boolean openQuote = false;
		int length = source.length();
		for (int i = 0; i < length; i++) {
			char c = source.charAt(i);
			if (c == ' ' && !openQuote) {
				parts.add(builder.toString());
				builder.setLength(0);
			} else if (c == '"') {
				openQuote = !openQuote;
			} else {
				builder.append(c);
			}
		}
		parts.add(builder.toString());
		return parts;
	}

}
