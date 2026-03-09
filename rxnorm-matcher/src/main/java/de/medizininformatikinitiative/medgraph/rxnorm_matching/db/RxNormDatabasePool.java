package de.medizininformatikinitiative.medgraph.rxnorm_matching.db;

import de.medizininformatikinitiative.medgraph.rxnorm_matching.model.DetailedRxNormSCD;

import java.util.Set;
import java.util.function.Supplier;

/**
 * {@link RxNormDatabase} delegate implementation which uses a pool of database instances to provide round-robin style
 * load balancing.
 *
 * @author Markus Budeus
 */
public class RxNormDatabasePool implements RxNormDatabase {

	private RxNormDatabase[] connections;
	private int index = -1;

	public RxNormDatabasePool(
			Supplier<RxNormDatabase> connectionBuilder,
			int connections
	) {
		this.connections = new RxNormDatabase[connections];

		for (int i = 0; i < this.connections.length; i++) {
			this.connections[i] = connectionBuilder.get();
		}
	}

	@Override
	public Set<String> getSCDRxCUIsForIngredientRxCUIs(Set<String> ingredientRxCUIs) {
		return getNext().getSCDRxCUIsForIngredientRxCUIs(ingredientRxCUIs);
	}

	@Override
	public Set<DetailedRxNormSCD> resolveDetails(Set<String> scdRxCUIs) {
		return getNext().resolveDetails(scdRxCUIs);
	}

	private synchronized RxNormDatabase getNext() {
		index = (index + 1) % connections.length;
		return connections[index];
	}

}
