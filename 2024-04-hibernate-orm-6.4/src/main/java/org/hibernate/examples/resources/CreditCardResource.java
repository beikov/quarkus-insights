package org.hibernate.examples.resources;

import java.util.List;

import org.hibernate.examples.entities.CreditCard;
import org.hibernate.examples.entities.Customer;
import org.hibernate.examples.repository.CreditCardRepository;
import org.hibernate.examples.representation.CreditCardResponseDto;
import org.hibernate.examples.representation.CreditCardUpsertDto;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path("credit-cards")
public class CreditCardResource {

	@Inject
	CreditCardRepository creditCardRepository;
	@HeaderParam("Customer")
	@Parameter(name = "Customer")
	Long customerId;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<CreditCardResponseDto> getAll() {
		return creditCardRepository.findAll( customerId )
				.stream()
				.map( this::toResponseDto )
				.toList();
	}

	@GET
	@Path( "{number}" )
	@Produces(MediaType.APPLICATION_JSON)
	public CreditCardResponseDto getCreditCard(@PathParam("number") String number) {
		try {
			var creditCard = creditCardRepository.findByNumberAndCustomerId( number, customerId );
			return toResponseDto( creditCard );
		}
		catch (NoResultException e) {
			throw new WebApplicationException( e, Response.Status.NOT_FOUND );
		}
	}

	@PUT
	@Path( "{number}" )
	@Transactional
	@Consumes(MediaType.APPLICATION_JSON)
	public Response upsert(@PathParam("number") String number, CreditCardUpsertDto dto) {
		var creditCard = new CreditCard();
		creditCard.number = number;
		creditCard.holderName = dto.holderName();
		creditCard.csc = dto.csc();
		creditCard.customer = new Customer();
		creditCard.customer.id = customerId;
//		creditCard.creationDate = Instant.now();
		creditCardRepository.upsert( creditCard );
		return Response.ok().build();
	}

	private CreditCardResponseDto toResponseDto(CreditCard creditCard) {
		return new CreditCardResponseDto(
				creditCard.number,
				creditCard.holderName,
				null//creditCard.creationDate
		);
	}

}
