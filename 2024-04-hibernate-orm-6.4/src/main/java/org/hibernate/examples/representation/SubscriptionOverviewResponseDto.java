package org.hibernate.examples.representation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record SubscriptionOverviewResponseDto(
		Long id,
		Instant creationDate,
		Instant subscriptionStart,
		Duration subscriptionInterval,
		List<SubscriptionItemOverviewResponseDto> subscriptionItemsOverview) {
}
