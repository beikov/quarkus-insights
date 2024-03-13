package org.hibernate.examples.resources;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.examples.entities.Product;
import org.hibernate.examples.repository.ProductRepository;
import org.hibernate.examples.representation.ProductQueryDto;
import org.hibernate.examples.representation.ProductResponseDto;
import org.hibernate.examples.representation.ProductVariantResponseDto;

import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("products")
public class ProductResource {

	@Inject
	ProductRepository productRepository;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ProductResponseDto> findProducts(@BeanParam ProductQueryDto dto) {
		return productRepository.findAllByQuery( dto )
				.stream()
				.map( ProductResource::toResponseDto )
				.toList();
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ProductResponseDto getProduct(@PathParam("id") Long id) {
		var product = productRepository.findByIdWithVariants( id );
		return toResponseDto( product );
	}

	private static ProductResponseDto toResponseDto(Product p) {
		return new ProductResponseDto(
				p.id,
				p.name,
				p.productType,
				p.category == null ? null : p.category.id,
				p.variants.stream()
						.map( pv -> new ProductVariantResponseDto(
								pv.id,
								pv.externalId,
								pv.name,
								pv.details == null ? null : pv.details.description(),
								pv.details == null ? null : pv.details.color(),
								pv.typeDetails,
								pv.price
						) )
						.toList()
		);
	}
}
