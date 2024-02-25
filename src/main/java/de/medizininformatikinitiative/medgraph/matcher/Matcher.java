package de.medizininformatikinitiative.medgraph.matcher;

import de.medizininformatikinitiative.medgraph.matcher.algorithm.MastersThesisAlgorithm;
import de.medizininformatikinitiative.medgraph.matcher.algorithm.MatchingAlgorithm;
import de.medizininformatikinitiative.medgraph.matcher.data.SubSortingTree;
import de.medizininformatikinitiative.medgraph.matcher.model.*;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Full Search Algorithm implementation with all three major steps.
 *
 * @author Markus Budeus
 */
public class Matcher {

	private final MatchingAlgorithm algorithm;
	private final BestMatchTransformer bestMatchTransformer;
	private final FinalResultTransformer finalResultTransformer;

	public Matcher(Session session) {
		algorithm = new MastersThesisAlgorithm(session);
		bestMatchTransformer = new BestMatchTransformer(session);
		finalResultTransformer = new FinalResultTransformer(session);
	}

	public ResultSet<FinalMatchingTarget> performMatching(String searchTerm) {
		HouselistEntry entry = new HouselistEntry();
		entry.searchTerm = searchTerm;
		SubSortingTree<MatchingTarget> result = algorithm.match(entry);
		ResultSet<ProductWithPzn> resultSet = toResultSet(result, bestMatchTransformer);
		return finalResultTransformer.transform(resultSet);
	}

	public static ResultSet<ProductWithPzn> toResultSet(SubSortingTree<MatchingTarget> results, BestMatchTransformer bestMatchTransformer) {
		List<MatchingTarget> topResults = results.getTopContents();
		List<MatchingTarget> otherResults = results.getContents();

		if (otherResults.isEmpty()) return new ResultSet<>(null, List.of(), List.of());

		otherResults = otherResults.subList(topResults.size(), otherResults.size());
		List<ProductWithPzn> transformedTargets = bestMatchTransformer.reorderAndTransform(results.getContents()
				.stream().filter(f -> f instanceof Product).map(f -> (Product) f).toList()
		);

		Set<Long> topMmiIds = topResults.stream().map(MatchingTarget::getMmiId).collect(Collectors.toSet());

		List<ProductWithPzn> sortedTransformedTopTargets = new ArrayList<>(topResults.size());
		for (ProductWithPzn target: transformedTargets) {
			if (topMmiIds.contains(target.getMmiId())) {
				sortedTransformedTopTargets.add(target);
			}
		}
		List<ProductWithPzn> transformedOtherTargets = new ArrayList<>(otherResults.size());
		for (MatchingTarget target: otherResults) {
			for (ProductWithPzn t: transformedTargets) {
				if (t.getMmiId() == target.getMmiId()) {
					transformedOtherTargets.add(t);
				}
			}
		}

		ProductWithPzn best = sortedTransformedTopTargets.get(0);
		sortedTransformedTopTargets.remove(0);

		return new ResultSet<>(best, sortedTransformedTopTargets, transformedOtherTargets);
	}

}
