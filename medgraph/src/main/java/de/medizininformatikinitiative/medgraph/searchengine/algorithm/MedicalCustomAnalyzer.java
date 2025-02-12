package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;

import java.io.IOException;
import java.util.List;

public class MedicalCustomAnalyzer extends Analyzer {

    private static final List<String> STOP_WORDS = List.of("mg", "tablet", "ml", "mg", "MM");


    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer(); // Zerlegt den Text in Tokens
        CharArraySet stopWordSet = new CharArraySet(STOP_WORDS, true);
        // Token-Stream-Filter
        TokenStream filter = new LowerCaseFilter(tokenizer); // Konvertiert in Kleinbuchstaben
        filter = new StopFilter(filter, stopWordSet); // Entfernt Stoppwörte

        // Synonym-Filter
        try {
            SynonymMap.Builder synonymMapBuilder = new SynonymMap.Builder(true);
            synonymMapBuilder.add(new CharsRef("ibuprofen"), new CharsRef("ibuprofenum"), true);
            synonymMapBuilder.add(new CharsRef("ibuprofenum"), new CharsRef("ibuprofen"), true);

            synonymMapBuilder.add(new CharsRef("dexa"), new CharsRef("dexamethason"), true);
            synonymMapBuilder.add(new CharsRef("dexamethason"), new CharsRef("dexa"), true);

            synonymMapBuilder.add(new CharsRef("hct"), new CharsRef("hydrochlorothiazid"), true);
            synonymMapBuilder.add(new CharsRef("hydrochlorothiazid"), new CharsRef("hct"), true);

            synonymMapBuilder.add(new CharsRef("ibu"), new CharsRef("ibuprofen"), true);
            synonymMapBuilder.add(new CharsRef("ibuprofen"), new CharsRef("ibu"), true);

            synonymMapBuilder.add(new CharsRef("kcl"), new CharsRef("kaliumchlorid"), true);
            synonymMapBuilder.add(new CharsRef("kaliumchlorid"), new CharsRef("kcl"), true);

            synonymMapBuilder.add(new CharsRef("mcp"), new CharsRef("metoclopramid"), true);
            synonymMapBuilder.add(new CharsRef("metoclopramid"), new CharsRef("mcp"), true);

            synonymMapBuilder.add(new CharsRef("nacl"), new CharsRef("natriumchlorid"), true);
            synonymMapBuilder.add(new CharsRef("natriumchlorid"), new CharsRef("nacl"), true);

            synonymMapBuilder.add(new CharsRef("omep"), new CharsRef("omeprazol"), true);
            synonymMapBuilder.add(new CharsRef("omeprazol"), new CharsRef("omep"), true);

            synonymMapBuilder.add(new CharsRef("simva"), new CharsRef("simvastatin"), true);
            synonymMapBuilder.add(new CharsRef("simvastatin"), new CharsRef("simva"), true);

            synonymMapBuilder.add(new CharsRef("vitamin b1"), new CharsRef("thiamin"), true);
            synonymMapBuilder.add(new CharsRef("thiamin"), new CharsRef("vitamin b1"), true);

            synonymMapBuilder.add(new CharsRef("rivarox"), new CharsRef("rivaroxaban"), true);
            synonymMapBuilder.add(new CharsRef("rivaroxaban"), new CharsRef("rivarox"), true);

                SynonymMap synonymMap = synonymMapBuilder.build();
            filter = new SynonymFilter(filter, synonymMap, true);
        } catch (IOException e) {
            throw new RuntimeException("Error creating SynonymMap", e);
        }

        return new TokenStreamComponents(tokenizer, filter);
    }
}