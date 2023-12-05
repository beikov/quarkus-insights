package org.hibernate.examples.representation;

import java.util.List;

import org.hibernate.examples.entities.ProductType;

public record ProductResponseDto(
		Long id,
		String name,
		ProductType productType,
		Long productCategoryId,
		List<ProductVariantResponseDto> variants) {
}
