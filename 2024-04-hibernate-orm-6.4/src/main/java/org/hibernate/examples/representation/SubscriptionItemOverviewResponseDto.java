package org.hibernate.examples.representation;

import java.math.BigDecimal;

public record SubscriptionItemOverviewResponseDto(
		Long productVariantId,
		String productVariantName,
		BigDecimal quantity) {
}
