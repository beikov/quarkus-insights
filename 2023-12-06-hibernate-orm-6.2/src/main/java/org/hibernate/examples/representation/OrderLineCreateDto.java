package org.hibernate.examples.representation;

import java.math.BigDecimal;

public record OrderLineCreateDto(Long productVariantId, BigDecimal quantity) {
}
