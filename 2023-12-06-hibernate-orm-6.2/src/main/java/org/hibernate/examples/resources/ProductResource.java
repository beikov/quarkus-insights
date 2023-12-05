package org.hibernate.examples.resources;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.examples.entities.Product;
import org.hibernate.examples.entities.ProductCategory;
import org.hibernate.examples.entities.ProductCategory_;
import org.hibernate.examples.entities.ProductVariant;
import org.hibernate.examples.entities.Product_;
import org.hibernate.examples.representation.ProductQueryDto;
import org.hibernate.examples.representation.ProductResponseDto;
import org.hibernate.examples.representation.ProductVariantResponseDto;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import org.hibernate.query.criteria.JpaSetJoin;

import jakarta.inject.Inject;
import jakarta.persistence.criteria.Predicate;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("products")
public class ProductResource {

	@Inject
	Session session;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ProductResponseDto> findProducts(@BeanParam ProductQueryDto dto) {
		var cb = session.getCriteriaBuilder();
		var cq = cb.createQuery( Product.class );
		var root = cq.from( Product.class );
		root.fetch( Product_.variants );
		cq.orderBy( cb.asc( root.get( Product_.id ) ) );
		applyFilter( cq, root, cb, dto );
		// Don't copy the criteria query as we ensure it is immutable
		session.setProperty( AvailableSettings.CRITERIA_COPY_TREE, false );
		return session.createQuery( cq )
				.getResultList()
				.stream()
				.map( ProductResource::toResponseDto )
				.toList();
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ProductResponseDto getProduct(@PathParam("id") Long id) {
		var product = session.createQuery( "from Product p join fetch p.variants where p.id = :id", Product.class )
				.setParameter( "id", id )
				.getSingleResult();
		return toResponseDto( product );
	}

	private void applyFilter(
			JpaCriteriaQuery<Product> cq,
			JpaRoot<Product> root,
			HibernateCriteriaBuilder cb,
			ProductQueryDto dto) {
		if ( dto.isEmpty() ) {
			return;
		}
		applyProductFilters( cq, root, cb, dto );
		applyProductVariantFilters( cq, root, cb, dto );
	}

	private static void applyProductFilters(
			JpaCriteriaQuery<Product> cq,
			JpaRoot<Product> root,
			HibernateCriteriaBuilder cb,
			ProductQueryDto dto) {
		var predicates = new ArrayList<Predicate>();
		if ( dto.categoryId() != null ) {
			var cteBase = cb.createTupleQuery();
			var categoryRoot = cteBase.from( ProductCategory.class );
			cteBase.multiselect( categoryRoot.get( ProductCategory_.id ).alias( "id" ) );
			cteBase.where(
					cb.equal(
							categoryRoot.get( ProductCategory_.id ),
							dto.categoryId()
					)
			);
			var transitiveChildCategories = cq.withRecursiveUnionDistinct(
					cteBase,
					cte -> {
						var cteUnion = cb.createTupleQuery();
						var cteRoot = cteUnion.from( cte );
						var childCategory = cteRoot.join( ProductCategory.class );
						childCategory.on(
								cb.equal(
										cteRoot.get( "id" ),
										childCategory.get( ProductCategory_.parentCategory ).get( ProductCategory_.id )
								)
						);
						cteUnion.multiselect( childCategory.get( ProductCategory_.id ).alias( "id" ) );

						return cteUnion;
					}
			);
			var childCategoryIdsSubquery = cq.subquery( Long.class );
			var parentCategory = childCategoryIdsSubquery.from( transitiveChildCategories );
			childCategoryIdsSubquery.select( parentCategory.get( "id" ) );
			predicates.add(
					cb.equal(
							root.get( Product_.category ).get( ProductCategory_.id ),
							cb.any( childCategoryIdsSubquery )
					)
			);
		}
		if ( !predicates.isEmpty() ) {
			if ( cq.getRestriction() != null ) {
				predicates.add( 0, cq.getRestriction() );
			}
			cq.where( predicates.toArray( Predicate[]::new ) );
		}
	}

	private static void applyProductVariantFilters(
			JpaCriteriaQuery<Product> cq,
			JpaRoot<Product> root,
			HibernateCriteriaBuilder cb,
			ProductQueryDto dto) {
		JpaSetJoin<Product, ProductVariant> variant = null;
		var subquery = cq.subquery( Integer.class );
		var correlatedRoot = subquery.correlate( root );
		var subqueryPredicates = new ArrayList<Predicate>();
		if ( dto.lowerPrice() != null ) {
			variant = variant != null ? variant : correlatedRoot.join( Product_.variants );
			subqueryPredicates.add( cb.ge(
					variant.get( "price" ).get( "amount" ),
					dto.lowerPrice()
			) );
		}
		if ( dto.upperPrice() != null ) {
			variant = variant != null ? variant : correlatedRoot.join( Product_.variants );
			subqueryPredicates.add( cb.le(
					variant.get( "price" ).get( "amount" ),
					dto.upperPrice()
			) );
		}
		if ( !subqueryPredicates.isEmpty() ) {
			subquery.select( cb.literal( 1 ) );
			subquery.where( cb.and( subqueryPredicates.toArray( Predicate[]::new ) ) );
			if ( cq.getRestriction() != null ) {
				cq.where( cb.and( cq.getRestriction(), cb.exists( subquery ) ) );
			}
			else {
				cq.where( cb.exists( subquery ) );
			}
		}
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
