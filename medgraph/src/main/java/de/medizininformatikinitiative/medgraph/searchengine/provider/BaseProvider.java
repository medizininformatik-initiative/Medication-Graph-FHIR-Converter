package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A provider for identifiers against which we can match.
 *
 * @param <S> the type of identifiers provided
 * @param <T> the type of objects described by the identifiers
 * @author Markus Budeus
 */
public class BaseProvider<S, T extends Identifiable> implements MappedIdentifierStream<S, T> {

	public static <S, T extends Identifiable> BaseProvider<S, T> ofIdentifiers(Collection<? extends MappedIdentifier<S, T>> identifiers) {
		return new BaseProvider<>(new ArrayList<>(identifiers));
	}

	public static <S, T extends Identifiable> BaseProvider<S, T> ofMatchingTargets(Stream<T> targets,
	                                                    Function<T, S> identifierExtractor) {
		return new BaseProvider<>(targets.map(t -> new MappedIdentifier<>(identifierExtractor.apply(t), t)).toList());
	}


	public static <T extends Identifiable> BaseProvider<String, T> ofIdentifiableNames(Collection<T> targets) {
		return BaseProvider.ofIdentifiables(targets, Identifiable::getName);
	}

	public static <S, T extends Identifiable> BaseProvider<S, T> ofIdentifiables(Collection<T> targets,
	                                                  Function<Identifiable, S> identifierExtractor) {
		List<MappedIdentifier<S, T>> resultList = new ArrayList<>(targets.size());
		targets.forEach(t -> resultList.add(new MappedIdentifier<>(identifierExtractor.apply(t), t)));
		return new BaseProvider<>(resultList);
	}

	public final List<MappedIdentifier<S, T>> identifiers;

	private BaseProvider(List<? extends MappedIdentifier<S, T>> identifiers) {
		this.identifiers = new ArrayList<>(identifiers);
	}

	@Override
	public Stream<MappedIdentifier<S, T>> getIdentifiers() {
		return identifiers.stream();
	}

}
