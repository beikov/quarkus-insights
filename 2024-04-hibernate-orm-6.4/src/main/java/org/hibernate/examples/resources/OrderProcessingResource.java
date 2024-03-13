package org.hibernate.examples.resources;

import java.util.List;

import org.hibernate.examples.entities.Order;
import org.hibernate.examples.entities.OrderStatus;
import org.hibernate.examples.repository.OrderRepository;
import org.hibernate.examples.representation.OrderLineResponseDto;
import org.hibernate.examples.representation.OrderResponseDto;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("admin/orders")
public class OrderProcessingResource {

	@Inject
	OrderRepository orderRepository;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<OrderResponseDto> getAllOrders() {
		return orderRepository.findAllWithOrderLines()
				.stream()
				.map( OrderProcessingResource::toResponseDto )
				.toList();
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public OrderResponseDto getOrder(@PathParam("id") Long id) {
		try {
			var order = orderRepository.findByIdWithOrderLines( id );
			return toResponseDto( order );
		}
		catch (NoResultException e) {
			throw new WebApplicationException( e, Response.Status.NOT_FOUND );
		}
	}

	@POST
	@Transactional
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response startOrder(@PathParam("id") Long id) {
		var order = orderRepository.findById( id );
		if ( order == null ) {
			throw new WebApplicationException( Response.Status.NOT_FOUND );
		}
		if ( order.status != OrderStatus.OPEN ) {
			throw new WebApplicationException( Response.Status.CONFLICT );
		}
		order.status = OrderStatus.IN_PROGRESS;
		return Response.ok().build();
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
