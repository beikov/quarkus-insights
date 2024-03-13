package org.hibernate.examples.representation;

import java.time.Duration;
import java.time.Instant;
import java.time.Month;
import java.time.ZoneOffset;

public record CustomerOrderingIntervalResponseDto(
		String yearQuarter,
		Month month,
		Duration averageIntervalInSeconds) {

	CustomerOrderingIntervalResponseDto(
			String yearQuarter,
			Instant instant,
			Duration averageIntervalInSeconds) {
		this( yearQuarter, instant == null ? null : Month.from( instant.atOffset( ZoneOffset.UTC ) ), averageIntervalInSeconds );
	}
}
