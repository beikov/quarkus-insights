package org.hibernate.examples.representation;

import java.math.BigDecimal;
import javax.money.MonetaryAmount;

public record OrderLineResponseDto(
		Long productVariantId,
		BigDecimal quantity,
		MonetaryAmount netAmount,
		BigDecimal taxPercent,
		MonetaryAmount totalAmount) {
}
