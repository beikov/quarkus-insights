package org.hibernate.examples.representation;

import java.math.BigDecimal;

public record CustomerDto(
		String firstName,
		String lastName,
		BigDecimal[] taxCategoryPercents) {
}
