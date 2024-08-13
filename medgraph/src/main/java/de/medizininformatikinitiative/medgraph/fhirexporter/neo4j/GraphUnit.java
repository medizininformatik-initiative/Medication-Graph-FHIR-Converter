package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

/**
 * Represents a unit, possibly with UCUM information.
 *
 * @param name the official, computer-readable name of this unit
 * @param mmiCode the code of this unit in the MMI Pharmindex
 * @param mmiName the name of this unit in the MMI Pharmindex
 * @param ucumCs the case-sensitive ucum code of this unit or null if no UCUM code is known
 * @param ucumCi the case-insensitive ucum code of this unit or null if no UCUM code is known
 * @param print the human-readable name of this unit
 *
 * @author Markus Budeus
 */
public record GraphUnit(@NotNull String name, @NotNull String mmiCode, @NotNull String mmiName, @Nullable String ucumCs, @Nullable String ucumCi, @Nullable String print) {

	public static final String NAME = "name";
	public static final String MMI_CODE = "mmiCode";
	public static final String MMI_NAME = "mmiName";
	public static final String UCUM_CS = "ucumCs";
	public static final String UCUM_CI = "ucumCi";
	public static final String UNIT_PRINT = "unitPrint";

	/**
	 * If the given value represents null (i.e. {@link Value#isNull()} returns true), this returns null.
	 * Otherwise, this returns a new {@link GraphUnit}.
	 */
	@Nullable
	public static GraphUnit from(Value value) {
		if (value.isNull()) return null;
		return new GraphUnit(value);
	}

	public GraphUnit(String name, String mmiCode, String mmiName, String ucumCs, String ucumCi, String print) {
		this.name = name;
		this.mmiCode = mmiCode;
		this.mmiName = mmiName;
		this.ucumCs = ucumCs;
		this.ucumCi = ucumCi;
		this.print = print != null ? print : mmiName;
	}

	public GraphUnit(MapAccessorWithDefaultValue value) {
		this(value.get(NAME).asString(),
				value.get(MMI_CODE).asString(),
				value.get(MMI_NAME, (String) null),
				value.get(UCUM_CS, (String) null),
				value.get(UCUM_CI, (String) null),
				value.get(UNIT_PRINT, (String) null));
	}

}
