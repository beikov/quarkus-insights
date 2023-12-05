package org.hibernate.examples.representation;

import java.time.Instant;

public record OrderOverviewResponseDto(
		Long id,
		Instant creationDate,
		String orderLinesOverview) {
}
