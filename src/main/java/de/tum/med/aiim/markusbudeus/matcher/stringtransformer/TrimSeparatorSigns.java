package de.tum.med.aiim.markusbudeus.matcher.stringtransformer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This transformer removes separator characters (',', ';', '|', '-', ':', '.') from the end of each string if present.
 * If this were to leave a string empty, it is removed from the set.
 */
public class TrimSeparatorSigns implements Transformer<List<String>, List<String>> {

	@Override
	public List<String> transform(List<String> source) {
		return source.stream().map(this::trimSeperators)
		             .filter(s -> !s.isBlank())
		             .toList();
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
