package de.tum.med.aiim.markusbudeus.fhirexporter.resource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

public class Quantity implements RatioOrQuantity {

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
				case EXACT -> Comparator.LESS;
				case LESS -> Comparator.LESS;
				case LESS_OR_EQUAL -> Comparator.LESS;
				case GREATER_OR_EQUAL -> null;
				case GREATER -> null;
			};
			case LESS_OR_EQUAL -> switch (c2) {
				case EXACT -> Comparator.LESS_OR_EQUAL;
				case LESS -> Comparator.LESS;
				case LESS_OR_EQUAL -> Comparator.LESS_OR_EQUAL;
				case GREATER_OR_EQUAL -> null;
				case GREATER -> null;
			};
			case GREATER_OR_EQUAL -> switch (c2) {
				case EXACT -> Comparator.GREATER_OR_EQUAL;
				case LESS -> null;
				case LESS_OR_EQUAL -> null;
				case GREATER_OR_EQUAL -> Comparator.GREATER_OR_EQUAL;
				case GREATER -> Comparator.GREATER;
			};
			case GREATER -> switch (c2) {
				case EXACT -> Comparator.GREATER;
				case LESS -> null;
				case LESS_OR_EQUAL -> null;
				case GREATER_OR_EQUAL -> Comparator.GREATER;
				case GREATER -> Comparator.GREATER;
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

}
