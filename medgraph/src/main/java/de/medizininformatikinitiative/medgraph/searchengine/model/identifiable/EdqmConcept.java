package de.medizininformatikinitiative.medgraph.searchengine.model.identifiable;

import de.medizininformatikinitiative.medgraph.common.EDQM;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a concept from the EDQM Standard terms.
 *
 * @author Markus Budeus
 */
public class EdqmConcept implements Matchable {

	/**
	 * The code (including a class-specific prefix, i.e. PDF-00000001) of this concept.
	 */
	@NotNull
	private final String code;

	/**
	 * The official (english) name of this concept.
	 */
	@NotNull
	private final String name;

	/**
	 * The type of this Standard Terms concept. (E.g. Pharmaceutical dose dorm, Release characteristic)
	 */
	@NotNull
	private final EDQM conceptType;

	public EdqmConcept(@NotNull String code, @NotNull String name, @NotNull EDQM conceptType) {
		this.code = conceptType.validateAndCorrectCode(code);
		this.name = name;
		this.conceptType = conceptType;
	}

	@NotNull
	public String getCode() {
		return code;
	}

	@Override
	@NotNull
	public String getName() {
		return name;
	}

	@NotNull
	public EDQM getConceptType() {
		return conceptType;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		EdqmConcept that = (EdqmConcept) object;
		return Objects.equals(code, that.code) && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code);
	}
}
