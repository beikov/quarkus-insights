package org.hibernate.examples.resources;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.examples.entities.Order;
import org.hibernate.examples.entities.OrderLine;
import org.hibernate.examples.entities.ProductVariant;
import org.hibernate.examples.repository.CustomerRepository;
import org.hibernate.examples.repository.OrderRepository;
import org.hibernate.examples.repository.ProductVariantRepository;
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
	OrderRepository orderRepository;
	@Inject
	CustomerRepository customerRepository;
	@Inject
	ProductVariantRepository productVariantRepository;
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
		return orderRepository.findAllOrdersForOverview( customerId );
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public OrderResponseDto getOrder(@PathParam("id") Long id) {
		try {
			var order = orderRepository.findByCustomerIdAndIdWithOrderLines( customerId, id );
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
		order.customer = customerRepository.findById( customerId );
		order.customerId = customerId;
		order.orderLines = new ArrayList<>();
		var taxCategoryPercents = order.customer.taxCategoryPercents;
		// Pre-load product variants in a batch and find by id later below
		productVariantRepository.findAllById(
				dto.orderLines()
						.stream()
						.map( OrderLineCreateDto::productVariantId )
						.toArray( Long[]::new )
		);
		for ( OrderLineCreateDto orderLineDto : dto.orderLines() ) {
			var productVariant = productVariantRepository.findById( orderLineDto.productVariantId() );
			order.orderLines.add(
					new OrderLine(
							productVariant,
							orderLineDto.quantity(),
							taxCategoryPercents[productVariant.taxCategory().ordinal()]
					)
			);
		}
		orderRepository.persist( order );
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
