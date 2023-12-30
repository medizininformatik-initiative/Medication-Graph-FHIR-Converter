package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.matcher.identifiermatcher.IIdentifierMatcher;
import de.tum.med.aiim.markusbudeus.matcher.provider.*;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.Filter;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.ResultTransformer;
import de.tum.med.aiim.markusbudeus.matcher.stringtransformer.Transformer;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OngoingMatching {

	public final HouselistEntry entry;
	private IdentifierProvider<String> currentMatches;

	public OngoingMatching(HouselistEntry entry, IdentifierProvider<String> baseProvider) {
		this.entry = entry;
		this.currentMatches = baseProvider;
	}

	public boolean narrowDownUnlessEmpty(
			Function<IdentifierProvider<String>, IIdentifierMatcher<String>> matcherConstructor) {
		return narrowDownUnlessEmpty(Transformer.IDENTITY, matcherConstructor);
	}

	public <S> boolean narrowDownUnlessEmpty(Transformer<String, S> transformer,
	                                         Function<IdentifierProvider<S>, IIdentifierMatcher<S>> matcherConstructor) {
		IIdentifierMatcher<S> matcher = matcherConstructor.apply(currentMatches.transform(transformer));
		return narrowDownUnlessEmpty(matcher.findMatch(entry.name).getBestMatches());
	}

	/**
	 * Narrows down the matches to the given set of identifiers, unless it's empty.
	 *
	 * @return true if the set of identifiers is not empty and thus the result was narrowed down, false otherwise
	 */
	public boolean narrowDownUnlessEmpty(Set<? extends MappedIdentifier<?>> newIdentifiers) {
		if (newIdentifiers.isEmpty()) return false;
		narrowDown(newIdentifiers);
		return true;
	}

	/**
	 * Narrows down the matches to the given set of identifiers, even if it's empty!
	 */
	public void narrowDown(Set<? extends MappedIdentifier<?>> newIdentifiers) {
		currentMatches = BaseProvider.ofIdentifiers(
				newIdentifiers.stream()
				              .map(MappedIdentifier::toBaseIdentifier)
				              .collect(Collectors.toSet()));
	}

	public void transformResults(ResultTransformer resultTransformer) {
		transformResults(resultTransformer, false);
	}

	public boolean transformResults(ResultTransformer resultTransformer, boolean onlyIfNotEmpty) {
		Map<IdentifierTarget, Set<IdentifierTarget>> transformationResults = new HashMap<>();
		List<IdentifierTarget> allTargets = currentMatches
				.getIdentifiers()
				.values()
				.stream()
				.flatMap(stringMappedIdentifier -> stringMappedIdentifier.targets.stream())
				.toList();
		List<Set<IdentifierTarget>> transformedIdentifiers = resultTransformer.batchTransform(allTargets, entry);
		for (int i = 0; i < allTargets.size(); i++) {
			transformationResults.put(allTargets.get(i), transformedIdentifiers.get(i));
		}

		if (onlyIfNotEmpty) {
			boolean empty = true;
			for (Set<IdentifierTarget> targets : transformedIdentifiers) {
				if (!targets.isEmpty()) {
					empty = false;
					break;
				}
			}
			if (empty)
				return false;
		}

		currentMatches.getIdentifiers().replaceAll((identifierName, mappedIdentifier) -> {
			MappedBaseIdentifier newIdentifier = new MappedBaseIdentifier(identifierName);
			mappedIdentifier.targets.forEach(target ->
					newIdentifier.targets.addAll(transformationResults.get(target)));
			return newIdentifier;
		});
		return true;
	}

	public void applyFilter(Filter filter) {
		transformResults(filter);
	}

	public IdentifierProvider<String> getCurrentMatchesProvider() {
		return currentMatches;
	}

	/**
	 * Takes all current {@link IdentifierTarget}s indirectly stored in the matching and assigns each one to a
	 * completely new identifier. For example, if you had the identifier "Tranexamsäure" pointing to that very substance
	 * and then you transform the {@link IdentifierTarget}s which are substances to products, then you are left with a
	 * "Tranexamsäure"-identifier, which points to a set of products. But now you may want to have these products
	 * identified by their name, not the substance name. Thus, you use this function and pass a function which extracts
	 * the product name.
	 */
	public void reassignIdentifiers(Function<IdentifierTarget, String> identifierExtractor) {
		currentMatches = BaseProvider.ofIdentifiers(
				getCurrentMatches()
						.stream()
						.map(target -> new MappedBaseIdentifier(identifierExtractor.apply(target), target))
						.collect(Collectors.toSet())
		);
	}

	public Set<IdentifierTarget> getCurrentMatches() {
		Set<IdentifierTarget> set = new HashSet<>();
		currentMatches.getIdentifiers().values().forEach(i -> set.addAll(i.targets));
		return set;
	}

}
