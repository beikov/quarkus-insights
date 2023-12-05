package org.hibernate.examples.entities;

import java.math.BigDecimal;

public record ProductVariantTypeDetails(
		BigDecimal alcoholPercent,
		BigDecimal sugarPercent,
		BigDecimal quantityAmount,
		QuantityUnit quantityUnit) {
}
