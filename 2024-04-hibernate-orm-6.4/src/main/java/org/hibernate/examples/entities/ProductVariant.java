package org.hibernate.examples.entities;

import java.math.BigDecimal;
import java.util.UUID;
import javax.money.MonetaryAmount;

import org.hibernate.annotations.CompositeType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.examples.types.MonetaryAmountType;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_variants")
public class ProductVariant {
	@Id
	@GeneratedValue
	public Long id;

	@Column(nullable = false)
	public UUID externalId;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public Product product;
	@Column(nullable = false)
	public String name;
	@Column(nullable = false)
	public ProductVariantDetails details;
	@Column(nullable = false)
	@JdbcTypeCode(SqlTypes.JSON)
	public ProductVariantTypeDetails typeDetails;
	@CompositeType(MonetaryAmountType.class)
	@AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false))
	@AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false))
	public MonetaryAmount price;

	public TaxCategory taxCategory() {
		return switch ( product.productType ) {
			case FOOD -> TaxCategory.FOOD;
			case DRINK -> {
				if ( typeDetails.sugarPercent() == null
						|| typeDetails.sugarPercent().compareTo( BigDecimal.TEN ) >= 0 ) {
					yield TaxCategory.DRINK_SWEETENED;
				}
				else {
					yield TaxCategory.DRINK_UNSWEETENED;
				}
			}
			case ALCOHOL -> TaxCategory.ALCOHOL;
			case OTHER -> TaxCategory.OTHER;
		};
	}
}
