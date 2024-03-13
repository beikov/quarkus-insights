package org.hibernate.examples.resources;

import org.hibernate.examples.repository.CustomerRepository;
import org.hibernate.examples.representation.CustomerDto;

import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path("profile")
public class ProfileResource {

	@HeaderParam("Customer")
	@Parameter(name = "Customer")
	Long customerId;

	@Inject
	CustomerRepository customerRepository;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public CustomerDto getCustomer() {
		var customer = customerRepository.findById( customerId );
		if ( customer == null ) {
			throw new WebApplicationException( Response.Status.NOT_FOUND );
		}
		return new CustomerDto( customer.firstName, customer.lastName, customer.taxCategoryPercents );
	}

	@DELETE
	@Transactional
	public Response removeCustomer() {
		try {
			customerRepository.remove( customerId );
			return Response.ok().build();
		}
		catch (EntityNotFoundException e) {
			throw new WebApplicationException( e, Response.Status.NOT_FOUND );
		}
	}
}
