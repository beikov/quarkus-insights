package org.hibernate.examples.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hibernate.examples.entities.Subscription;
import org.hibernate.examples.repository.SubscriptionRepository;
import org.hibernate.examples.representation.SubscriptionItemOverviewResponseDto;
import org.hibernate.examples.representation.SubscriptionOverviewResponseDto;
import org.hibernate.query.Order;
import org.hibernate.query.Page;
import org.hibernate.query.SortDirection;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path("subscriptions")
public class SubscriptionResource {

	@Inject
	SubscriptionRepository subscriptionRepository;
	@HeaderParam("Customer")
	@Parameter(name = "Customer")
	Long customerId;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<SubscriptionOverviewResponseDto> getSubscriptions(
			@QueryParam("page") @DefaultValue("1") int page,
			@QueryParam("size") @DefaultValue("10") int size,
			@QueryParam("sort") List<String> sort
	) {
		var orders = toOrders( Subscription.class, sort, "id", SortDirection.DESCENDING );
		return subscriptionRepository.findAll( customerId, Page.page( size, page ), orders )
				.stream()
				.map( s -> new SubscriptionOverviewResponseDto(
						s.id,
						s.creationDate,
						s.subscriptionStart,
						s.subscriptionInterval,
						s.subscriptionItems.stream()
								.map( si -> new SubscriptionItemOverviewResponseDto(
										si.productVariant.id,
										si.productVariant.name,
										si.quantity
								) )
								.toList()
				) )
				.toList();
	}

	private <X> List<Order<X>> toOrders(
			Class<X> entityClass,
			List<String> sort,
			String idAttributeName,
			SortDirection idSortDirection) {
		var attributes = new HashSet<String>( sort.size() + 1 );
		var orders = new ArrayList<Order<X>>( sort.size() + 1 );
		for ( String s : sort ) {
			final int comma = s.lastIndexOf( ',' );
			final String attributeName;
			final SortDirection sortDirection;
			if ( comma == -1 ) {
				attributeName = s;
				sortDirection = SortDirection.ASCENDING;
			}
			else {
				attributeName = s.substring( 0, comma );
				sortDirection = SortDirection.interpret( s.substring( comma + 1 ) );
			}
			if ( attributes.add( attributeName ) ) {
				orders.add( Order.by( entityClass, attributeName, sortDirection ) );
			}
		}

		if ( attributes.add( idAttributeName ) ) {
			orders.add( Order.by( entityClass, idAttributeName, idSortDirection ) );
		}
		return orders;
	}
}
