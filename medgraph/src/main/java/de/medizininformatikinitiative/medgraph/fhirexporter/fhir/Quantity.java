package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class is an implementation of the FHIR R4 Quantity object.
 *
 * @author Markus Budeus
 */
public class Quantity implements RatioOrQuantity {

	public static final String UCUM_URI = "http://unitsofmeasure.org";
	public static final String NEUTRAL_UNIT_CODE = "1";

	public static Quantity one() {
		Quantity quantity = new Quantity();
		quantity.code = NEUTRAL_UNIT_CODE;
		quantity.system = UCUM_URI;
		quantity.value = BigDecimal.ONE;
		return quantity;
	}

	public BigDecimal value;
	private String comparator;
	public String unit;
	public String system;
	/**
	 * A computer processable form of the unit in some unit representation system.
	 */
	public String code;

	public String getComparator() {
		return comparator;
	}

	public void setComparator(Comparator comparator) {
		if (comparator == null || comparator == Comparator.EXACT) {
			this.comparator = null;
		} else {
			this.comparator = comparator.value;
		}
	}

	@Override
	public RatioOrQuantity plus(RatioOrQuantity other) {
		if (other instanceof Ratio) {
			return other.plus(this);
		} else if (other instanceof Quantity) {
			return plus((Quantity) other);
		}
		return null;
	}

	public Quantity plus(Quantity other) {

		if (!Objects.equals(other.system, this.system)) return null;
		if (!Objects.equals(other.code, this.code)) return null;
		Comparator resultComparator = combineComparators(getComparatorEnum(), other.getComparatorEnum());
		if (resultComparator == null) return null;

		Quantity result = new Quantity();
		result.system = this.system;
		result.code = this.code;
		result.value = this.value.add(other.value);
		result.setComparator(resultComparator);

		return result;

	}

	private Comparator combineComparators(Comparator c1, Comparator c2) {
		return switch (c1) {
			case EXACT -> switch (c2) {
				case EXACT -> Comparator.EXACT;
				case LESS -> Comparator.LESS;
				case LESS_OR_EQUAL -> Comparator.LESS_OR_EQUAL;
				case GREATER_OR_EQUAL -> Comparator.GREATER_OR_EQUAL;
				case GREATER -> Comparator.GREATER;
			};
			case LESS -> switch (c2) {
				case EXACT, LESS, LESS_OR_EQUAL -> Comparator.LESS;
				case GREATER_OR_EQUAL, GREATER -> null;
			};
			case LESS_OR_EQUAL -> switch (c2) {
				case EXACT, LESS_OR_EQUAL -> Comparator.LESS_OR_EQUAL;
				case LESS -> Comparator.LESS;
				case GREATER_OR_EQUAL, GREATER -> null;
			};
			case GREATER_OR_EQUAL -> switch (c2) {
				case EXACT, GREATER_OR_EQUAL -> Comparator.GREATER_OR_EQUAL;
				case LESS, LESS_OR_EQUAL -> null;
				case GREATER -> Comparator.GREATER;
			};
			case GREATER -> switch (c2) {
				case EXACT, GREATER_OR_EQUAL, GREATER -> Comparator.GREATER;
				case LESS, LESS_OR_EQUAL -> null;
			};
		};
	}

	public enum Comparator {
		EXACT("="),
		LESS("<"),
		LESS_OR_EQUAL("<="),
		GREATER_OR_EQUAL(">="),
		GREATER(">");

		private final String value;

		Comparator(String value) {
			this.value = value;
		}

		static Comparator byValue(String value) {
			return Arrays.stream(Comparator.values()).filter(c -> c.value.equals(value)).findFirst().orElse(null);
		}

	}

	private Comparator getComparatorEnum() {
		if (comparator == null) return Comparator.EXACT;
		else return Comparator.byValue(comparator);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		Quantity quantity = (Quantity) object;
		return Objects.equals(value, quantity.value) && Objects.equals(comparator,
				quantity.comparator) && Objects.equals(unit, quantity.unit) && Objects.equals(system,
				quantity.system) && Objects.equals(code, quantity.code);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, comparator, unit, system, code);
	}
}
