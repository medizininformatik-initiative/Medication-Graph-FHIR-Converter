package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

/**
 * This class is an implementation of the FHIR R4 CodeableConcept object.
 *
 * @author Markus Budeus
 */
@Deprecated
public class CodeableConcept {

	/**
	 * Code defined by a terminology system
	 */
	public Coding[] coding;
	/**
	 * Plain text representation of the concept
	 */
	public String text;

}
