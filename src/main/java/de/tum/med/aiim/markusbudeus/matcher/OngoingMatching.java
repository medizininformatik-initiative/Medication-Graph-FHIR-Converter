package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.matcher.identifiermatcher.Match;
import de.tum.med.aiim.markusbudeus.matcher.provider.BaseProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedBaseIdentifier;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.Filter;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.ResultTransformer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OngoingMatching {

	public final HouselistEntry entry;
	private BaseProvider currentMatches;

	public OngoingMatching(HouselistEntry entry, BaseProvider baseProvider) {
		this.entry = entry;
		this.currentMatches = baseProvider;
	}

	public void narrowDown(Function<BaseProvider, Set<MappedIdentifier<?>>> matcherInvocation) {
		narrowDown(matcherInvocation.apply(currentMatches));
	}

	public void narrowDown(Set<MappedIdentifier<?>> newIdentifiers) {
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

	public BaseProvider getCurrentMatches() {
		return currentMatches;
	}

}
