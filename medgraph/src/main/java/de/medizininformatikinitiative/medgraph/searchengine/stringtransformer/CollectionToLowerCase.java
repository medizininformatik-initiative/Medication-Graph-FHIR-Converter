package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import java.util.Collection;
import java.util.List;

/**
 * This transformer makes the names lowercase.
 *
 * @author Markus Budeus
 */
public class CollectionToLowerCase<T extends Collection<String>> implements Transformer<T, List<String>> {

	@Override
	public List<String> apply(T source) {
		return source.stream().map(String::toLowerCase).toList();
	}

}
