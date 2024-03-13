package org.hibernate.examples.representation;

import java.time.Instant;
import java.util.List;

public record OrderResponseDto(
		Long id,
		Instant creationDate,
		List<OrderLineResponseDto> orderLines) {
}
