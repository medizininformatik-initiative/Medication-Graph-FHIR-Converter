package de.tum.med.aiim.markusbudeus.matcher.resultranker;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.resulttransformer.Filter;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

public class FilterProductsWithoutSuccessor implements Filter {

	/**
	 * If true, {@link MatchingTarget}s which are not products pass this filter.
	 */
	static final boolean ALLOW_NON_PRODUCTS = true;

	private final Session session;

	public FilterProductsWithoutSuccessor(Session session) {
		this.session = session;
	}

	@Override
	public List<Boolean> batchPassesFilter(List<MatchingTarget> targets, HouselistEntry entry) {
		Set<Long> filterPasses = downloadSuccessorlessProducts(
				targets.stream()
				       .filter(t -> t.getType() == MatchingTarget.Type.PRODUCT)
				       .map(MatchingTarget::getMmiId).toList())
				.collect(Collectors.toSet());

		List<Boolean> resultList = new ArrayList<>();
		for (MatchingTarget t : targets) {
			if (t.getType() == MatchingTarget.Type.PRODUCT) {
				resultList.add(filterPasses.contains(t.getMmiId()));
			} else {
				resultList.add(ALLOW_NON_PRODUCTS);
			}
		}

		return resultList;
	}

	@Override
	public boolean passesFilter(MatchingTarget target, HouselistEntry entry) {
		if (target.getType() != MatchingTarget.Type.PRODUCT) return ALLOW_NON_PRODUCTS;
		return downloadSuccessorlessProducts(List.of(target.getMmiId()))
				.findFirst()
				.isPresent();
	}

	/**
	 * Returns a stream of ids of all products in the given input list for which no successor products exist, unless the
	 * successor product is also the predecessor for a different package.
	 */
	private synchronized Stream<Long> downloadSuccessorlessProducts(List<Long> productMmiIds) {
		Result result = session.run(new Query(
				"MATCH (p1:" + PRODUCT_LABEL + ")\n" +
						"WHERE p1.mmiId IN $mmiIds\n" +
						"OPTIONAL MATCH (p1)<-[:" + PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]-(:" + PACKAGE_LABEL + ")" +
						"-[r1:" + PACKAGE_HAS_SUCCESSOR_LABEL + "]->(:" + PACKAGE_LABEL + ")" +
						"-[:" + PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]->(p2:" + PRODUCT_LABEL + ")\n" +
						"OPTIONAL MATCH (p1)<-[:" + PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]-(:" + PACKAGE_LABEL + ")" +
						"<-[r2:" + PACKAGE_HAS_SUCCESSOR_LABEL + "]-(:" + PACKAGE_LABEL + ")" +
						"-[:" + PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]->(p2)\n" +
						"WITH p1 WHERE r1 IS NULL OR NOT r2 IS NULL\n" +
						"RETURN DISTINCT p1.mmiId",
				parameters("mmiIds", productMmiIds)
		));

		return result.stream().map(r -> r.get(0).asLong());
	}

}
