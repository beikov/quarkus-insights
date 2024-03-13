package org.hibernate.examples.representation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.ws.rs.QueryParam;

public class ProductQueryDto {
	@QueryParam("lowerPrice")
	private BigDecimal lowerPrice;
	@QueryParam("upperPrice")
	private BigDecimal upperPrice;
	@QueryParam("categoryId")
	private Long categoryId;
	@QueryParam("tags")
	private Set<String> tags;

	public ProductQueryDto() {
	}

	public boolean isEmpty() {
		return lowerPrice == null
				&& upperPrice == null
				&& categoryId == null
				&& ( tags == null || tags.isEmpty() );
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

	public Set<String> getTags() {
		return tags;
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
				Objects.equals( this.categoryId, that.categoryId ) &&
				Objects.equals( this.tags, that.tags );
	}

	@Override
	public int hashCode() {
		return Objects.hash( lowerPrice, upperPrice, categoryId, tags );
	}

	@Override
	public String toString() {
		return "ProductQueryDto[" +
				"lowerPrice=" + lowerPrice + ", " +
				"upperPrice=" + upperPrice + ", " +
				"categoryId=" + categoryId + ", " +
				"tags=" + tags + ']';
	}

}
