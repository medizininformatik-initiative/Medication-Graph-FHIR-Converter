package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.BaseProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedBaseIdentifier;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.Filter;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.ResultTransformer;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class OngoingMatching {

	// TODO Support calling matchers with current matches again

	public final HouselistEntry entry;
	private BaseProvider currentMatches;

	public OngoingMatching(HouselistEntry entry, BaseProvider baseProvider) {
		this.entry = entry;
		this.currentMatches = baseProvider;
	}

	public boolean narrowDownUnlessEmpty(
			BiFunction<HouselistEntry, BaseProvider, Set<MappedIdentifier<?>>> matcherInvocation) {
		return narrowDownUnlessEmpty(matcherInvocation.apply(entry, currentMatches));
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
				.identifiers
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

		currentMatches.identifiers.replaceAll((identifierName, mappedIdentifier) -> {
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

	public BaseProvider getCurrentMatchesProvider() {
		return currentMatches;
	}

	public Set<IdentifierTarget> getCurrentMatches() {
		Set<IdentifierTarget> set = new HashSet<>();
		currentMatches.identifiers.values().forEach(i -> set.addAll(i.targets));
		return set;
	}

}
