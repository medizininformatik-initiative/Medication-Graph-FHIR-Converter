package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A provider for identifiers against which we can match.
 *
 * @param <S> the type of identifiers provided.
 * @author Markus Budeus
 */
public class BaseProvider<S> implements IdentifierStream<S> {

	public static <S> BaseProvider<S> ofIdentifiers(Collection<MappedIdentifier<S>> identifiers) {
		return new BaseProvider<>(new ArrayList<>(identifiers));
	}

	public static <S> BaseProvider<S> ofMatchingTargets(Stream<Matchable> targets,
	                                                    Function<Matchable, S> identifierExtractor) {
		return new BaseProvider<>(targets.map(t -> new MappedIdentifier<>(identifierExtractor.apply(t), t)).toList());
	}


	public static BaseProvider<String> ofIdentifiableNames(Collection<Identifiable> targets) {
		return BaseProvider.ofIdentifiables(targets, Identifiable::getName);
	}

	public static <S> BaseProvider<S> ofIdentifiables(Collection<Identifiable> targets,
	                                                  Function<Identifiable, S> identifierExtractor) {
		List<MappedIdentifier<S>> resultList = new ArrayList<>(targets.size());
		targets.forEach(t -> resultList.add(new MappedIdentifier<>(identifierExtractor.apply(t), t)));
		return new BaseProvider<>(resultList);
	}

	public final List<MappedIdentifier<S>> identifiers;

	private BaseProvider(List<MappedIdentifier<S>> identifiers) {
		this.identifiers = identifiers;
	}

	@Override
	public Stream<MappedIdentifier<S>> getIdentifiers() {
		return identifiers.stream();
	}

}
