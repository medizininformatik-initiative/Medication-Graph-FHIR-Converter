package de.medizininformatikinitiative.medgraph.matcher.houselisttransformer;

import de.medizininformatikinitiative.medgraph.matcher.model.HouselistEntry;

/**
 * Houselist transformers make changes to a {@link HouselistEntry} used for searching in some way.
 *
 * @author Markus Budeus
 */
public interface HouselistTransformer {

	void transform(HouselistEntry entry);

}
