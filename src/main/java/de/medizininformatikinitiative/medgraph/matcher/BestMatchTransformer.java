package de.medizininformatikinitiative.medgraph.matcher;

import de.medizininformatikinitiative.medgraph.matcher.model.Product;
import de.medizininformatikinitiative.medgraph.matcher.model.ProductWithPzn;
import de.medizininformatikinitiative.medgraph.matcher.model.MatchingTarget;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

/**
 * This class deterministically orders a set of product matches and assigns a PZN to each product if known.
 *
 * @author Markus Budeus
 */
public class BestMatchTransformer {

	private final Session session;

	public BestMatchTransformer(Session session) {
		this.session = session;
	}

	public synchronized List<ProductWithPzn> reorderAndTransform(List<? extends Product> targets) {
		if (targets.isEmpty()) return null;

		Result r = session.run(new Query(
				"MATCH (p:"+PRODUCT_LABEL+")\n" +
						"WHERE p.mmiId IN $mmiIds\n" +
						"MATCH (pz:"+PZN_LABEL+")-[:"+CODE_REFERENCE_RELATIONSHIP_NAME+"]->" +
						"(pk:"+PACKAGE_LABEL+")-[:"+PACKAGE_BELONGS_TO_PRODUCT_LABEL+"]->(p)\n" +
						"WITH p.mmiId AS productId, p.name AS name, pz.code AS pzn\n" +
						"ORDER BY pk.onMarketDate DESC, p.name, pk.mmiId\n" +
						"RETURN productId, name, collect(pzn)[0]",
				parameters("mmiIds", targets.stream().map(Product::getMmiId).toList())
		));

		List<ProductWithPzn> productWithPzns = new ArrayList<>();

		while (r.hasNext()) {
			Record record = r.next();
			productWithPzns.add(
					new ProductWithPzn(record.get(0).asLong(), record.get(1).asString(), record.get(2).asString())
			);
		}
		return productWithPzns;
	}

}
