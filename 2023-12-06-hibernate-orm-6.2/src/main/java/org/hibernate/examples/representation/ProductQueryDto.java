package org.hibernate.examples.representation;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.ws.rs.QueryParam;

public class ProductQueryDto {
	@QueryParam("lowerPrice")
	private BigDecimal lowerPrice;
	@QueryParam("upperPrice")
	private BigDecimal upperPrice;
	@QueryParam("categoryId")
	private Long categoryId;

	public ProductQueryDto() {
	}

	public boolean isEmpty() {
		return lowerPrice == null
				&& upperPrice == null
				&& categoryId == null;
	}

	public BigDecimal lowerPrice() {
		return lowerPrice;
	}

	public BigDecimal upperPrice() {
		return upperPrice;
	}

	public Long categoryId() {
		return categoryId;
	}

	@Override
	public boolean equals(Object obj) {
		if ( obj == this ) {
			return true;
		}
		if ( obj == null || obj.getClass() != this.getClass() ) {
			return false;
		}
		var that = (ProductQueryDto) obj;
		return Objects.equals( this.lowerPrice, that.lowerPrice ) &&
				Objects.equals( this.upperPrice, that.upperPrice ) &&
				Objects.equals( this.categoryId, that.categoryId );
	}

	@Override
	public int hashCode() {
		return Objects.hash( lowerPrice, upperPrice, categoryId );
	}

	@Override
	public String toString() {
		return "ProductQueryDto[" +
				"lowerPrice=" + lowerPrice + ", " +
				"upperPrice=" + upperPrice + ", " +
				"categoryId=" + categoryId + ']';
	}

}
