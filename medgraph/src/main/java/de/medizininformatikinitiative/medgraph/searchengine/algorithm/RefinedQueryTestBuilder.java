package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TrackableIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmConcept;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement;

import java.util.List;

public class RefinedQueryTestBuilder {

    /**
     * Erstellt ein Beispiel-RefinedQuery-Objekt für Performance-Tests.
     * @param productKeyword Ein Keyword, das als Produktname verwendet wird.
     * @return Ein RefinedQuery-Objekt mit Testwerten.
     */
    public static RefinedQuery createExampleQuery(String productKeyword) {
        // Erstelle eine Liste von Produktname-Keywords
        TrackableIdentifier<List<String>> productNameKeywords = new OriginalIdentifier<>(
                List.of(productKeyword), OriginalIdentifier.Source.SEARCH_QUERY);

        // Leere Listen oder Platzhalter-Objekte für die anderen Parameter
        List<MatchingObject<Substance>> substances = List.of();
        List<MatchingObject<Dosage>> dosages = List.of();
        List<MatchingObject<Amount>> drugAmounts = List.of();
        List<MatchingObject<EdqmPharmaceuticalDoseForm>> doseForms = List.of();
        List<MatchingObject<EdqmConcept>> doseFormCharacteristics = List.of();

        // `@Nullable`-Parameter können auf `null` gesetzt werden
        SubstringUsageStatement dosageUsageStatement = null;
        SubstringUsageStatement dosageGeneralSearchTermUsageStatement = null;
        SubstringUsageStatement doseFormUsageStatement = null;
        SubstringUsageStatement doseFormGeneralSearchTermUsageStatement = null;
        SubstringUsageStatement substanceUsageStatement = null;
        SubstringUsageStatement substanceGeneralSearchTermUsageStatement = null;

        // Konstruktoraufruf mit den Beispielwerten
        return new RefinedQuery(
                productNameKeywords,
                substances,
                dosages,
                drugAmounts,
                doseForms,
                doseFormCharacteristics,
                dosageUsageStatement,
                dosageGeneralSearchTermUsageStatement,
                doseFormUsageStatement,
                doseFormGeneralSearchTermUsageStatement,
                substanceUsageStatement,
                substanceGeneralSearchTermUsageStatement
        );
    }
}
