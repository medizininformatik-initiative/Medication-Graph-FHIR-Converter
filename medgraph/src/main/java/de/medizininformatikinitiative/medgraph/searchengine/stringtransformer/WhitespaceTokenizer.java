package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.IntRange;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.MultiSubstringUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A transformer which splits a string along its whitespaces.
 *
 * @author Markus Budeus
 */
public class WhitespaceTokenizer implements TraceableTransformer<String, List<String>,
		MultiSubstringUsageStatement, StringListUsageStatement> {

	private final boolean considerQuotes;

	public WhitespaceTokenizer() {
		this(true);
	}

	/**
	 * Creates a new {@link WhitespaceTokenizer}.
	 *
	 * @param considerQuotes if true, multiple words within double quotes '"' are not separated.
	 */
	public WhitespaceTokenizer(boolean considerQuotes) {
		this.considerQuotes = considerQuotes;
	}

	@Override
	public List<String> apply(String source) {
		return mapToValue(split(source));
	}

	private List<String> mapToValue(List<StringExtract> extracts) {
		return extracts
				.stream().map(e -> e.value)
				.toList();
	}

	/**
	 * Splits the source string and returns corresponding string extracts. Non-applicable extracts (those that do
	 * not pass the {@link #isApplicable(String)}-filter) are removed from the output.
	 */
	private List<StringExtract> split(String source) {
		StringBuilder builder = new StringBuilder(24);
		List<StringExtract> parts = new ArrayList<>();
		boolean openQuote = false;
		int wordStart = 0;
		int length = source.length();
		for (int i = 0; i < length; i++) {
			char c = source.charAt(i);
			if (c == ' ' && !openQuote) {
				// The range goes until i (excluding), which means the space is not part of the range.
				parts.add(new StringExtract(builder.toString(), new IntRange(wordStart, i)));
				builder.setLength(0);
				wordStart = i + 1;
			} else if (c == '"' && considerQuotes) {
				openQuote = !openQuote;
			} else {
				builder.append(c);
			}
		}
		parts.add(new StringExtract(builder.toString(), new IntRange(wordStart, length)));

		parts.removeIf(e -> !isApplicable(e.value));

		return parts;
	}

	@Override
	public MultiSubstringUsageStatement reverseTransformUsageStatement(String input,
	                                                              StringListUsageStatement usageStatement) {
		List<StringExtract> extracts = split(input);
		if (!mapToValue(extracts).equals(usageStatement.getOriginal())) {
			throw new IllegalArgumentException("Splitting the given input would not result in the original value " +
					"specified by the given usage statement!");
		}

		Tools.ensureValidity(this, input, usageStatement);

		Set<IntRange> usedRanges = new HashSet<>();
		Set<Integer> usedIndices = usageStatement.getUsedIndices();
		for (int i = 0; i < extracts.size(); i++) {
			if (usedIndices.contains(i)) {
				usedRanges.add(extracts.get(i).origin);
			}
		}

		return new MultiSubstringUsageStatement(input, usedRanges);
	}

	private static class StringExtract {
		final String value;
		/**
		 * Where in the original word this part has been found. Please note this range includes any quotes around the
		 * word.
		 * <p>
		 * E.g., when splitting 'Siamese cat', the ranges would be [0:7] for 'Siamese' and [8:10] for 'cat'.
		 * <p>
		 * When splitting 'Holy "Hand Grenade"' with quoting enabled, the ranges would be [0:4] for 'Holy' and [5:19]
		 * for 'Hand Grenade'
		 */
		final IntRange origin;

		private StringExtract(String value, IntRange origin) {
			this.value = value;
			this.origin = origin;
		}
	}

	/**
	 * Whether this string is an applicable output. Any non-blank string is applicable.
	 */
	private boolean isApplicable(String s) {
		return !s.isBlank();
	}
}
