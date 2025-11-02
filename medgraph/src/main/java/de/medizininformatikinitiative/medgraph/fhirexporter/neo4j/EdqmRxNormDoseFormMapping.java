package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.util.Objects;

/**
 * Represents a mapping from EDQM dose form to RxNorm dose form with additional
 * classification information including TRAC, RCA, AMEC, and ISIC codes.
 *
 * @author Lucy
 */
public class EdqmRxNormDoseFormMapping {

    private final String edqmDoseForm;
    private final String rxnormDoseForm;
    private final String tracCode;
    private final String tracTerm;
    private final String rcaCode;
    private final String rcaTerm;
    private final String amecCode;
    private final String amecTerm;
    private final String isicCode;
    private final String isicTerm;

    /**
     * Creates a mapping from Neo4j database record.
     */
    public EdqmRxNormDoseFormMapping(MapAccessorWithDefaultValue mapAccessor) {
        this.edqmDoseForm = mapAccessor.get("edqmDoseForm", (String) null);
        this.rxnormDoseForm = mapAccessor.get("rxnormDoseForm", (String) null);
        this.tracCode = mapAccessor.get("tracCode", (String) null);
        this.tracTerm = mapAccessor.get("tracTerm", (String) null);
        this.rcaCode = mapAccessor.get("rcaCode", (String) null);
        this.rcaTerm = mapAccessor.get("rcaTerm", (String) null);
        this.amecCode = mapAccessor.get("amecCode", (String) null);
        this.amecTerm = mapAccessor.get("amecTerm", (String) null);
        this.isicCode = mapAccessor.get("isicCode", (String) null);
        this.isicTerm = mapAccessor.get("isicTerm", (String) null);
    }

    /**
     * Creates a mapping from individual parameters.
     */
    public EdqmRxNormDoseFormMapping(String edqmDoseForm, String rxnormDoseForm,
                                   String tracCode, String tracTerm,
                                   String rcaCode, String rcaTerm,
                                   String amecCode, String amecTerm,
                                   String isicCode, String isicTerm) {
        this.edqmDoseForm = edqmDoseForm;
        this.rxnormDoseForm = rxnormDoseForm;
        this.tracCode = tracCode;
        this.tracTerm = tracTerm;
        this.rcaCode = rcaCode;
        this.rcaTerm = rcaTerm;
        this.amecCode = amecCode;
        this.amecTerm = amecTerm;
        this.isicCode = isicCode;
        this.isicTerm = isicTerm;
    }

    /**
     * Like the constructor, but can return null if the given value represents the null value.
     */
    @Nullable
    public static EdqmRxNormDoseFormMapping from(Value value) {
        if (value.isNull()) return null;
        return new EdqmRxNormDoseFormMapping(value);
    }

    // Getters
    public String getEdqmDoseForm() { return edqmDoseForm; }
    public String getRxnormDoseForm() { return rxnormDoseForm; }
    public String getTracCode() { return tracCode; }
    public String getTracTerm() { return tracTerm; }
    public String getRcaCode() { return rcaCode; }
    public String getRcaTerm() { return rcaTerm; }
    public String getAmecCode() { return amecCode; }
    public String getAmecTerm() { return amecTerm; }
    public String getIsicCode() { return isicCode; }
    public String getIsicTerm() { return isicTerm; }

    /**
     * Returns true if this mapping has a valid RxNorm dose form.
     */
    public boolean hasRxNormMapping() {
        return rxnormDoseForm != null && !rxnormDoseForm.trim().isEmpty();
    }

    /**
     * Returns true if this mapping has TRAC information.
     */
    public boolean hasTracInfo() {
        return tracCode != null || tracTerm != null;
    }

    /**
     * Returns true if this mapping has RCA information.
     */
    public boolean hasRcaInfo() {
        return rcaCode != null || rcaTerm != null;
    }

    /**
     * Returns true if this mapping has AMEC information.
     */
    public boolean hasAmecInfo() {
        return amecCode != null || amecTerm != null;
    }

    /**
     * Returns true if this mapping has ISIC information.
     */
    public boolean hasIsicInfo() {
        return isicCode != null || isicTerm != null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        EdqmRxNormDoseFormMapping that = (EdqmRxNormDoseFormMapping) object;
        return Objects.equals(edqmDoseForm, that.edqmDoseForm) &&
               Objects.equals(rxnormDoseForm, that.rxnormDoseForm) &&
               Objects.equals(tracCode, that.tracCode) &&
               Objects.equals(tracTerm, that.tracTerm) &&
               Objects.equals(rcaCode, that.rcaCode) &&
               Objects.equals(rcaTerm, that.rcaTerm) &&
               Objects.equals(amecCode, that.amecCode) &&
               Objects.equals(amecTerm, that.amecTerm) &&
               Objects.equals(isicCode, that.isicCode) &&
               Objects.equals(isicTerm, that.isicTerm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edqmDoseForm, rxnormDoseForm, tracCode, tracTerm, rcaCode, rcaTerm,
                          amecCode, amecTerm, isicCode, isicTerm);
    }

    @Override
    public String toString() {
        return "EdqmRxNormDoseFormMapping{" +
               "edqmDoseForm='" + edqmDoseForm + '\'' +
               ", rxnormDoseForm='" + rxnormDoseForm + '\'' +
               ", tracCode='" + tracCode + '\'' +
               ", tracTerm='" + tracTerm + '\'' +
               ", rcaCode='" + rcaCode + '\'' +
               ", rcaTerm='" + rcaTerm + '\'' +
               ", amecCode='" + amecCode + '\'' +
               ", amecTerm='" + amecTerm + '\'' +
               ", isicCode='" + isicCode + '\'' +
               ", isicTerm='" + isicTerm + '\'' +
               '}';
    }
}
