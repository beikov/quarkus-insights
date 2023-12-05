package org.hibernate.examples.representation;

import java.time.Duration;
import java.time.Month;

public record CustomerOrderingIntervalResponseDto(
		String yearQuarter,
		Month month,
		Duration averageIntervalInSeconds) {
}
