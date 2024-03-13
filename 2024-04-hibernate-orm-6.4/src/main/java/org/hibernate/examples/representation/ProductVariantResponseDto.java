package org.hibernate.examples.representation;

import java.util.UUID;
import javax.money.MonetaryAmount;

import org.hibernate.examples.entities.ProductVariantTypeDetails;

public record ProductVariantResponseDto(
		Long id,
		UUID externalId,
		String name,
		String description,
		String color,
		ProductVariantTypeDetails typeDetails,
		MonetaryAmount price) {
}
