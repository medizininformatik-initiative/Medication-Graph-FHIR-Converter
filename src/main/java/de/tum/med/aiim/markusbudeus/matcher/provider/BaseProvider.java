package de.tum.med.aiim.markusbudeus.matcher.provider;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public class BaseProvider implements IdentifierProvider<String> {

	public static BaseProvider ofDatabaseSynonymes() {
		try (DatabaseConnection connection = new DatabaseConnection();
		     Session session = connection.createSession()) {
			return new BaseProvider(downloadSynonymes(session));
		}
	}

	public static BaseProvider ofIdentifiers(Set<MappedIdentifier<String>> identifiers) {
		Map<String, MappedIdentifier<String>> map = new HashMap<>();
		identifiers.forEach(i -> map.put(i.identifier.getIdentifier(), i));
		return new BaseProvider(map);
	}

	private static Map<String, MappedIdentifier<String>> downloadSynonymes(Session session) {
		Map<String, MappedIdentifier<String>> result = new HashMap<>();
		Result res = session.run(
				"MATCH (sy:" + SYNONYME_LABEL + ")--(t) " +
						"RETURN sy.name, t.mmiId, t.name, labels(t)"
		);

		res.forEachRemaining(record -> {
			String synonymeName = record.get(0).asString();
			MappedIdentifier<String> identifier = result.computeIfAbsent(synonymeName, MappedBaseIdentifier::new);
			IdentifierTarget target = toSynonymeTarget(record);
			if (target != null) identifier.targets.add(target);
		});

		return result;
	}

	private static IdentifierTarget toSynonymeTarget(Record record) {
		long mmiId = record.get(1).asLong();
		String name = record.get(2).asString();
		List<String> labels = record.get(3).asList(Value::asString);
		if (labels.size() != 1) {
			System.err.println("Synonyme links to node with multiple labels! " + labels);
			return null;
		}
		IdentifierTarget.Type type = switch (labels.get(0)) {
			case PRODUCT_LABEL -> IdentifierTarget.Type.PRODUCT;
			case SUBSTANCE_LABEL -> IdentifierTarget.Type.SUBSTANCE;
			default -> null;
		};
		if (type == null) {
			System.err.println("Unexpected label on synonyme target: " + labels.get(0));
			return null;
		}
		return new IdentifierTarget(mmiId, name, type);
	}

	public final Map<String, MappedIdentifier<String>> identifiers;

	public BaseProvider(Map<String, MappedIdentifier<String>> identifiers) {
		this.identifiers = identifiers;
	}

	@Override
	public Map<String, MappedIdentifier<String>> getIdentifiers() {
		return identifiers;
	}

	@Override
	public String applyTransformation(String source) {
		return source;
	}

}
