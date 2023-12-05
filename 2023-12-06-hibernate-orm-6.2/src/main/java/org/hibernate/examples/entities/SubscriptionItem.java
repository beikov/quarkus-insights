package org.hibernate.examples.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Embeddable
public class SubscriptionItem {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public ProductVariant productVariant;
	@Column(nullable = false)
	public BigDecimal quantity;
}
