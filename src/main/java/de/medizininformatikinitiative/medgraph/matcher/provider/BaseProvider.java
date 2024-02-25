package de.medizininformatikinitiative.medgraph.matcher.provider;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.matcher.model.MatchingTarget;
import de.medizininformatikinitiative.medgraph.matcher.model.Product;
import de.medizininformatikinitiative.medgraph.matcher.model.Substance;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions.*;

/**
 * A provider for identifiers against which we can match.
 *
 * @param <S> the type of identifiers provided.
 * @author Markus Budeus
 */
public class BaseProvider<S> implements IdentifierProvider<S> {

	/**
	 * A provider for identifiers from the knowledge graph synonymes.
	 * @return a {@link BaseProvider} which contains all synonymes as identifers
	 */
	public static BaseProvider<String> ofDatabaseSynonymes() {
		try (DatabaseConnection connection = new DatabaseConnection();
		     Session session = connection.createSession()) {
			return BaseProvider.ofDatabaseSynonymes(session);
		}
	}

	/**
	 * A provider for identifiers from the knowledge graph synonymes.
	 * @param session the session which is used to download the synonymes
	 * @return a {@link BaseProvider} which contains all synonymes as identifers
	 */
	public static BaseProvider<String> ofDatabaseSynonymes(Session session) {
		return BaseProvider.ofIdentifiers(downloadSynonymes(session).collect(Collectors.toSet()));
	}

	public static <S> BaseProvider<S> ofIdentifiers(Collection<MappedIdentifier<S>> identifiers) {
		return new BaseProvider<>(new ArrayList<>(identifiers));
	}

	public static <S> BaseProvider<S> ofMatchingTargets(Stream<MatchingTarget> targets,
	                                                    Function<MatchingTarget, S> identifierExtractor) {
		return new BaseProvider<>(targets.map(t -> new MappedIdentifier<>(identifierExtractor.apply(t), t)).toList());
	}


	public static BaseProvider<String> ofMatchingTargetNames(Collection<MatchingTarget> targets) {
		return BaseProvider.ofMatchingTargets(targets, MatchingTarget::getName);
	}

	public static <S> BaseProvider<S> ofMatchingTargets(Collection<MatchingTarget> targets,
	                                                    Function<MatchingTarget, S> identifierExtractor) {
		List<MappedIdentifier<S>> resultList = new ArrayList<>(targets.size());
		targets.forEach(t -> resultList.add(new MappedIdentifier<>(identifierExtractor.apply(t), t)));
		return new BaseProvider<>(resultList);
	}

	private static Stream<MappedIdentifier<String>> downloadSynonymes(Session session) {
		return session.run(
				              "MATCH (sy:" + SYNONYME_LABEL + ")--(t) " +
						              "RETURN sy.name, t.mmiId, t.name, labels(t)"
		              )
		              .stream()
		              .map(BaseProvider::toSynonymeTarget);
	}

	private static MappedIdentifier<String> toSynonymeTarget(Record record) {
		long mmiId = record.get(1).asLong();
		String name = record.get(2).asString();
		List<String> labels = record.get(3).asList(Value::asString);
		if (labels.size() != 1) {
			System.err.println("Synonyme links to node with multiple labels! " + labels);
			return null;
		}
		MatchingTarget target = switch (labels.get(0)) {
			case PRODUCT_LABEL -> new Product(mmiId, name);
			case SUBSTANCE_LABEL -> new Substance(mmiId, name);
			default -> null;
		};
		if (target == null) {
			System.err.println("Unexpected label on synonyme target: " + labels.get(0));
			return null;
		}
		return new MappedIdentifier<>(record.get(0).asString(), target);
	}

	public final List<MappedIdentifier<S>> identifiers;

	private BaseProvider(List<MappedIdentifier<S>> identifiers) {
		this.identifiers = identifiers;
	}

	@Override
	public List<MappedIdentifier<S>> getIdentifiers() {
		return identifiers;
	}

}
