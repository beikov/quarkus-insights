package org.hibernate.examples.representation;

public record CreditCardUpsertDto(
		String holderName,
		String csc) {
}
