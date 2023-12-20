package de.tum.med.aiim.markusbudeus.matcher;

import java.math.BigDecimal;

public class Amount {

	public final BigDecimal number;
	public final String unit;

	public Amount(BigDecimal number, String unit) {
		this.number = number;
		this.unit = unit;
	}
}
