package de.tum.med.aiim.markusbudeus.matcher.matchers;


import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.TransformedProvider;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.Transformer;

import java.util.function.Function;

/**
 * Class which carries information about steps to be taken before a matching is attempted
 *
 * @param <S> the type of search term which is given to the matcher
 * @param <T> the type of identifiers the matcher matches against
 */
public class MatcherConfiguration<S, T> {

	/**
	 * Creates a simple {@link MatcherConfiguration} which uses the name from the houselist entry and directly
	 * matches against the identifiers of the given {@link IdentifierProvider}.
	 */
	public static <S> MatcherConfiguration<String, S> getBareboneConfig(IdentifierProvider<S> provider) {
		return new MatcherConfiguration<>(e -> e.searchTerm, provider);
	}

	/**
	 * Creates a simple matcher configuration which matches using the name of the houselist entry with a given
	 * transformation. The same transformation is also applied to the identifiers from the given provider.
	 * @param transformer the transformations to apply
	 * @param provider the against whose terms to match
	 * @return a corresponding {@link MatcherConfiguration}
	 */
	public static <S> MatcherConfiguration<S, S> usingTransformations(Transformer<String, S> transformer,
	                                                                  IdentifierProvider<String> provider) {
		return usingTransformations(e -> e.searchTerm, provider, transformer);
	}

	/**
	 * Creates a {@link MatcherConfiguration} which works by applying a set of transformations to both the feature
	 * extracted from the house list and the identifiers given by the given provider.
	 */
	public static <S, V> MatcherConfiguration<S, S> usingTransformations(Function<HouselistEntry, V> featureExtractor,
	                                                                     IdentifierProvider<V> provider,
	                                                                     Transformer<V, S> transformer) {
		return new MatcherConfiguration<>(
				e -> transformer.transform(featureExtractor.apply(e)),
				new TransformedProvider<>(provider, transformer)
		);
	}

	private Function<HouselistEntry, S> featureExtractor;
	private IdentifierProvider<T> identifierProvider;

	public MatcherConfiguration(Function<HouselistEntry, S> featureExtractor,
	                            IdentifierProvider<T> identifierProvider) {
		if (featureExtractor == null || identifierProvider == null)
			throw new NullPointerException();
		this.featureExtractor = featureExtractor;
		this.identifierProvider = identifierProvider;
	}

	public Function<HouselistEntry, S> getFeatureExtractor() {
		return featureExtractor;
	}

	public void setFeatureExtractor(
			Function<HouselistEntry, S> featureExtractor) {
		if (featureExtractor == null)
			throw new NullPointerException();
		this.featureExtractor = featureExtractor;
	}

	public IdentifierProvider<T> getIdentifierProvider() {
		return identifierProvider;
	}

	public void setIdentifierProvider(IdentifierProvider<T> identifierProvider) {
		if (identifierProvider == null)
			throw new NullPointerException();
		this.identifierProvider = identifierProvider;
	}
}
