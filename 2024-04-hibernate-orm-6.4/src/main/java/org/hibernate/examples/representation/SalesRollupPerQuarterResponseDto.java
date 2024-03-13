package org.hibernate.examples.representation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Month;
import java.time.ZoneOffset;

import org.hibernate.annotations.Imported;

@Imported
public record SalesRollupPerQuarterResponseDto(
		String yearQuarter,
		Month month,
		BigDecimal amountSold) {

	SalesRollupPerQuarterResponseDto(
			String yearQuarter,
			Instant instant,
			BigDecimal amountSold) {
		this( yearQuarter, instant == null ? null : Month.from( instant.atOffset( ZoneOffset.UTC ) ), amountSold );
	}
}
