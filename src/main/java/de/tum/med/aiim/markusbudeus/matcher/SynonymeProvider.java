package de.tum.med.aiim.markusbudeus.matcher;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;

import java.util.*;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public class SynonymeProvider {

	public static void main(String[] args) {
		List<String> strings = new ArrayList<>();
		strings.add("A");
		strings.add("Test");
		System.out.println(strings);
	}

	public final Map<String, Synonyme> synonymes;

	public SynonymeProvider(Session session) {
		this.synonymes = downloadSynonymes(session);
	}

	private Map<String, Synonyme> downloadSynonymes(Session session) {
		Map<String, Synonyme> result = new HashMap<>();
		Result res = session.run(
				"MATCH (sy:" + SYNONYME_LABEL + ")--(t) " +
						"RETURN sy.name, t.mmiId, t.name, labels(t)"
		);

		res.forEachRemaining(record -> {
			String synonymeName = record.get(0).asString();
			Synonyme synonyme = result.computeIfAbsent(synonymeName, Synonyme::new);
			SynonymeTarget target = toSynonymeTarget(record);
			if (target != null) synonyme.targets.add(target);
		});

		return result;
	}

	private SynonymeTarget toSynonymeTarget(Record record) {
		long mmiId = record.get(1).asLong();
		String name = record.get(2).asString();
		List<String> labels = record.get(3).asList(Value::asString);
		if (labels.size() != 1) {
			System.err.println("Synonyme links to node with multiple labels! " + labels);
			return null;
		}
		SynonymeTarget.Type type = switch (labels.get(0)) {
			case PRODUCT_LABEL -> SynonymeTarget.Type.PRODUCT;
			case SUBSTANCE_LABEL -> SynonymeTarget.Type.SUBSTANCE;
			default -> null;
		};
		if (type == null) {
			System.err.println("Unexpected label on synonyme target: "+labels.get(0));
			return null;
		}
		return new SynonymeTarget(mmiId, name, type);
	}

}
