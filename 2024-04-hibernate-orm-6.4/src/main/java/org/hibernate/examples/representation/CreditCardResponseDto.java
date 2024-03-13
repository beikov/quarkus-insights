package org.hibernate.examples.representation;

import java.time.Instant;

public record CreditCardResponseDto(
		String number,
		String holderName,
		Instant creationDate) {
}
