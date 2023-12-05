package org.hibernate.examples.entities;

import java.math.BigDecimal;
import javax.money.MonetaryAmount;

import org.hibernate.annotations.CompositeType;
import org.hibernate.examples.types.MonetaryAmountType;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Embeddable
public class OrderLine {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false, updatable = false)
	public ProductVariant productVariant;
	@Column(nullable = false, updatable = false)
	public BigDecimal quantity;
	@CompositeType(MonetaryAmountType.class)
	@AttributeOverride(name = "amount", column = @Column(name = "net_amount", nullable = false, updatable = false))
	@AttributeOverride(name = "currency", column = @Column(name = "net_currency", nullable = false, updatable = false))
	public MonetaryAmount netAmount;
	@Column(nullable = false, updatable = false)
	public BigDecimal taxPercent;
	@CompositeType(MonetaryAmountType.class)
	@AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false, updatable = false))
	@AttributeOverride(name = "currency", column = @Column(name = "total_currency", nullable = false, updatable = false))
	public MonetaryAmount totalAmount;

	public OrderLine() {
	}

	public OrderLine(ProductVariant productVariant, BigDecimal quantity, BigDecimal taxPercent) {
		this.productVariant = productVariant;
		this.quantity = quantity;
		this.taxPercent = taxPercent;
		this.netAmount = productVariant.price.multiply( quantity );
		this.totalAmount = this.netAmount.add( this.netAmount.multiply( taxPercent ).divide( 100 ) );
	}
}
