package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.*;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.Filter;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.ResultTransformer;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class OngoingMatching {

	public final HouselistEntry entry;
	private BaseProvider currentMatches;

	public OngoingMatching(HouselistEntry entry, BaseProvider baseProvider) {
		this.entry = entry;
		this.currentMatches = baseProvider;
	}

	public boolean narrowDownUnlessEmpty(BiFunction<HouselistEntry, BaseProvider, Set<MappedIdentifier<?>>> matcherInvocation) {
		return narrowDownUnlessEmpty(matcherInvocation.apply(entry, currentMatches));
	}

	/**
	 * Narrows down the matches to the given set of identifiers, unless it's empty.
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
		currentMatches.identifiers.replaceAll((identifierName, mappedIdentifier) -> {
			MappedBaseIdentifier newIdentifier = new MappedBaseIdentifier(identifierName);
			mappedIdentifier.targets.forEach(target ->
					newIdentifier.targets.addAll(resultTransformer.transform(target, entry)));
			return newIdentifier;
		});
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
