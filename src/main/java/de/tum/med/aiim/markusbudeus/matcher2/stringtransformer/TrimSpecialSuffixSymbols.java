package de.tum.med.aiim.markusbudeus.matcher2.stringtransformer;

import java.util.List;

/**
 * This transformer removes special characters (',', ';', '|', '-', ':', '.', '®') from the end of each string if
 * present. If this were to leave a string empty, it is removed from the set.
 */
public class TrimSpecialSuffixSymbols implements Transformer<List<String>, List<String>> {

	@Override
	public List<String> transform(List<String> source) {
		List<String> result = source.stream().map(this::trimSeperators)
		                            .filter(s -> !s.isBlank())
		                            .toList();
		if (result.isEmpty())
			System.err.println("Warning! List " + source + " became empty after trimming separators!");
		return result;
	}

	private String trimSeperators(String s) {
		int lastChar = s.length() - 1;
		while (lastChar >= 0) {
			switch (s.charAt(lastChar)) {
				case ',':
				case ';':
				case '|':
				case '-':
				case ':':
				case '.':
				case '®':
					s = s.substring(0, lastChar);
					lastChar -= 1;
					break;
				default:
					return s;
			}
		}
		return s;
	}

}
