package org.hibernate.examples.representation;

import java.math.BigDecimal;

import org.hibernate.annotations.Imported;

@Imported
public record TopProductPerQuarterResponseDto(
		String yearQuarter,
		Long productId,
		BigDecimal amountSold) {
}
