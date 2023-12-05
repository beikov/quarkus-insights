package org.hibernate.examples.representation;

import java.math.BigDecimal;
import java.time.Month;

import org.hibernate.annotations.Imported;

@Imported
public record SalesRollupPerQuarterResponseDto(
		String yearQuarter,
		Month month,
		BigDecimal amountSold) {
}
