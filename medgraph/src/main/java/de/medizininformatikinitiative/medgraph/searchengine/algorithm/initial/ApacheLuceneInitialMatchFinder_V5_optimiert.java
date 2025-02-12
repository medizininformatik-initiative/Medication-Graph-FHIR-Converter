package de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial;

import de.medizininformatikinitiative.medgraph.searchengine.algorithm.MedicalCustomAnalyzer;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Origin;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import org.apache.commons.lang3.StringUtils;

import org.apache.lucene.store.ByteBuffersDirectory;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.Closeable;
import java.io.IOException;
import java.util.stream.Stream;

import static org.neo4j.driver.Values.parameters;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import org.apache.lucene.document.StoredField;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * This class searches for products using an Apache Lucene-powered full text index. */

public class ApacheLuceneInitialMatchFinder_V5_optimiert implements InitialMatchFinder<Product>, Closeable {
    private final Session session;
    private final Directory index;

    private final DirectoryReader reader;
    private final IndexSearcher searcher;

    /**
     * Constructor    */
    public ApacheLuceneInitialMatchFinder_V5_optimiert(Session session) throws InterruptedException {
        this.session = session;
        this.index = new ByteBuffersDirectory();
        indexProducts();
        try {
            this.reader = DirectoryReader.open(index); // DirectoryReader one time opened
            this.searcher = new IndexSearcher(reader); // IndexSearcher one time created
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Lucene reader", e);
        }
    }


    private void indexProducts() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try (IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(new StandardAnalyzer()))) {
            Result result = session.run(
                    new org.neo4j.driver.Query(
                            "MATCH (p:Product) RETURN p.mmiId AS id, p.name AS name",
                            parameters()
                    )
            );

            result.stream().forEach(record -> {
                executor.submit(() -> {
                    Document doc = new Document();
                    long id = record.get("id").asLong();
                    String name = record.get("name").asString();

                    doc.add(new LongPoint("id", id)); // Indexiertes Feld für Filterung
                    doc.add(new StoredField("id", id)); // Gespeichertes Feld für Abrufbarkeit
                    doc.add(new TextField("name", name, TextField.Store.YES)); // Name als TextField für Volltextsuchen

                    try {
                        writer.addDocument(doc);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to index products", e);
        } finally {
            executor.shutdown(); // Warten, bis alle Threads abgeschlossen sind
            while (!executor.isTerminated()) {
                Thread.sleep(10);
            }
        }
    }

    @Override
    public Stream<OriginalMatch<Product>> findInitialMatches(SearchQuery query) {
        String searchTerm = QueryParser.escape(StringUtils.joinWith(" ", query.getProductNameKeywords()));
        try {
            TopDocs topDocs = searcher.search(new QueryParser("name", new MedicalCustomAnalyzer()).parse(searchTerm), 8);
            /* ... */
            return Stream.of(topDocs.scoreDocs).parallel()
                    .map(scoreDoc -> {
                        try {
                            Document doc = searcher.doc(scoreDoc.doc);
                            Product product = new Product(doc.getField("id").numericValue().longValue(), doc.get("name"));
                            return new OriginalMatch<>(product, 1.0, new ApacheLuceneOrigin());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException("Failed to search for products", e);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
        index.close(); // Schließt den Speicher und gibt Ressourcen frei
    }

    /**
     * Returns a stream which wraps all elements from the given source stream into {@link OriginalMatch}-instances.
     */

    /**    * Indicates the match was found by employing an Apache Lucene index.    */
    public static class ApacheLuceneOrigin implements Origin {

    }
}

