package de.medizininfomatikinitiative.medgraph.rxmatching;

import java.util.List;

/**
 * Represents a parsed RxNorm Semantic Clinical Drug (SCD).
 *
 * @param rxcui       The RxCUI of this SCD.
 * @param doseForm    The dose form assigned to this SCD, in RxNorm encoding.
 * @param ingredients The ingredients assigned to this SCD.
 * @author Markus Budeus
 */
public record RxNormCandidate(String rxcui, String doseForm, List<RxNormSCDC> ingredients) {}
