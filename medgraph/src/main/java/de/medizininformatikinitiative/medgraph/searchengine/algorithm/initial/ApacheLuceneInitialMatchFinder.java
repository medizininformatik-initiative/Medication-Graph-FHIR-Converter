package de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Origin;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.stream.Stream;

import static org.neo4j.driver.Values.parameters;

/**
 * This class searches for products using an Apache Lucene-powered full text index.
 */
public class ApacheLuceneInitialMatchFinder implements InitialMatchFinder<Product> {

	private final Session session;

	public ApacheLuceneInitialMatchFinder(Session session) {
		this.session = session;
	}

	@Override
	public Stream<OriginalMatch<Product>> findInitialMatches(SearchQuery query) {
		// Hallo Lucy! In dieser Methode kannst du deine Neo4j-Aufrufe einfügen und nach Produkten anhand
		// von Suchbegriffen suchen!
		// Ich habe dir eine Beispielimplementierung da gelassen, die du dann nach deinen Wünschen abändern kannst.
		// Selbstverständlich darfst du jederzeit weitere Klassen und Methoden hinzufügen und diese aufrufen.
		// Diese Git Branch gehört dir ;)

		// Hier erhältst du eine Liste von Schlüsselwörtern, nach denen du suchen darfst. Die anderen Methoden
		// Von SearchQuery kannst du an dieser Stelle ignorieren.
		// Die hier zurückgegebenen Schlüsselwörter sind alle Wörter, die in das Suchfeld eingegeben wurden, aber nicht
		// als Dosisangabe (z.B. "10ml"), Dosierungsform (z.B. "Tablette") oder Wirkstoff identifiziert wurden sowie
		// alle Wörter, die in der erweiterten Ansicht der Suche explizit als "Produkt" gesucht wurden.
		List<String> searchKeywords = query.getProductNameKeywords();

		// In diesem Fall setzen wir die Schlüsselwörter einfach stumpf wieder zusammen, mit Leerzeichen dazwischen.
		final String searchTerm = StringUtils.joinWith(" ", searchKeywords.toArray());

		// Und nun suchen wir in der Datenbank nach allen Produkten, die genau diesen zusammengesetzen Suchbegriff
		// im Namen haben.
		Result result = session.run(
				new Query(
						// Hier kannst du eine beliebige Cypher-Anfrage schreiben.
						"MATCH (p:Product) " +
								"WHERE p.name CONTAINS $searchTerm " +
								"RETURN p.mmiId AS id, p.name AS name",
						parameters("searchTerm", searchTerm)
				)
		);

		// Zum Schluss muss das Ergebnis in den richtigen Rückgabetyp verpackt werden.
		// Das passiert hier automatisch, solange die Neo4j-Anfrage eine Spalte "id" mit der mmiId des jeweiligen
		// Produkts sowie eine Spalte "name" mit dem Namen zurückliefert.
		//
		// Natürlich darfst du jederzeit andere Spalten zurückgeben und dann selbst verarbeiten.
		return wrapIntoOriginalMatch(convertToProductStream(result));
	}

	/**
	 * Transforms the given result into a stream of {@link Product}-instances. Assumes the result contains a column 'id'
	 * containing a number which is the product's MMI id and a column 'name' with the product's name.
	 */
	private Stream<Product> convertToProductStream(Result result) {
		return result.stream()
		             .map(record -> new Product(record.get("id").asLong(), record.get("name").asString()));

	}

	/**
	 * Returns a stream which wraps all elements from the given source stream into {@link OriginalMatch}-instances.
	 */
	private Stream<OriginalMatch<Product>> wrapIntoOriginalMatch(Stream<Product> stream) {
		return stream.map(product -> new OriginalMatch<>(product, 1.0, new ApacheLuceneOrigin()));
	}

	/**
	 * Indicates the match was found by employing an Apache Lucene index.
	 */
	public static class ApacheLuceneOrigin implements Origin {

	}
}
