package org.hibernate.examples.resources;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.examples.entities.Customer;
import org.hibernate.examples.entities.Order;
import org.hibernate.examples.entities.OrderLine;
import org.hibernate.examples.entities.ProductVariant;
import org.hibernate.examples.representation.OrderCreateDto;
import org.hibernate.examples.representation.OrderLineCreateDto;
import org.hibernate.examples.representation.OrderLineResponseDto;
import org.hibernate.examples.representation.OrderOverviewResponseDto;
import org.hibernate.examples.representation.OrderResponseDto;

import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path("orders")
public class OrderResource {

	@Inject
	Session session;
	@Inject
	RoutingContext context;
	@HeaderParam("Customer")
	@Parameter(name = "Customer")
	Long customerId;
	@Inject
	UriInfo uriInfo;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<OrderOverviewResponseDto> getAllOrders() {
		return session.createQuery(
						STR. """
						select new \{
										// workaround until https://github.com/quarkusio/quarkus/issues/37518 is fixed
										OrderOverviewResponseDto.class.getName()
										}(
							o.id,
							o.creationDate,
							listagg(
								trim(trailing '.' from trim(trailing '0' from cast(l.quantity as String)))
									|| 'x ' || l.productVariantName,
								', '
							) within group (order by l.orderLine)
								|| case when size(o.orderLines) > 2 then ', ...' else '' end
						)
						from Order o
						join lateral (
							select index(l) as orderLine, l.quantity as quantity, pv.name as productVariantName
							from o.orderLines l
							join l.productVariant pv
							order by orderLine
							limit 2
						) l
						where o.customer.id = :customerId
						group by o.id, o.customer.id
						order by o.creationDate desc, o.id desc
						""" ,
						OrderOverviewResponseDto.class
				)
				.setParameter( "customerId", customerId )
				.getResultList();
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public OrderResponseDto getOrder(@PathParam("id") Long id) {
		try {
			var order = session.createQuery(
							"from Order o join fetch o.orderLines where o.customer.id = :customerId and o.id = :id",
							Order.class
					)
					.setParameter( "customerId", customerId )
					.setParameter( "id", id )
					.getSingleResult();
			return toResponseDto( order );
		}
		catch (NoResultException e) {
			throw new WebApplicationException( e, Response.Status.NOT_FOUND );
		}
	}

	@POST
	@Transactional
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createOrder(OrderCreateDto dto) throws Exception {
		var order = new Order();
		order.createdFromAddress = InetAddress.getByName( context.request().remoteAddress().hostAddress() );
		order.customer = session.find( Customer.class, customerId );
		order.orderLines = new ArrayList<>();
		var taxCategoryPercents = order.customer.taxCategoryPercents;
		for ( OrderLineCreateDto orderLineDto : dto.orderLines() ) {
			var productVariant = session.find( ProductVariant.class, orderLineDto.productVariantId() );
			order.orderLines.add(
					new OrderLine(
							productVariant,
							orderLineDto.quantity(),
							taxCategoryPercents[productVariant.taxCategory().ordinal()]
					)
			);
		}
		session.persist( order );
		return Response.created( uriInfo.getBaseUri().resolve( "orders/" + order.id ) ).build();
	}

	private static OrderResponseDto toResponseDto(Order o) {
		return new OrderResponseDto(
				o.id,
				o.creationDate,
				o.orderLines.stream()
						.map( ol -> new OrderLineResponseDto(
								ol.productVariant.id,
								ol.quantity,
								ol.netAmount,
								ol.taxPercent,
								ol.totalAmount
						) )
						.toList()
		);
	}
}
