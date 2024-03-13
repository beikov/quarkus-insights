package org.hibernate.examples.resources;

import java.util.List;

import org.hibernate.examples.repository.StatisticsRepository;
import org.hibernate.examples.representation.CustomerOrderingIntervalResponseDto;
import org.hibernate.examples.representation.SalesRollupPerQuarterResponseDto;
import org.hibernate.examples.representation.TopProductPerQuarterResponseDto;
import org.hibernate.examples.representation.TopProductResponseDto;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("stats")
public class StatisticsResource {

	@Inject
	StatisticsRepository statisticsRepository;

	@GET
	@Path("top-products")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TopProductResponseDto> findTopProducts(@QueryParam("limit") @DefaultValue("3") Integer limit) {
		return statisticsRepository.findTopProducts( limit );
	}

	@GET
	@Path("top-products-by-quarter")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TopProductPerQuarterResponseDto> findTopProductsByQuarter(
			@QueryParam("quarters") @DefaultValue("4") Integer quarters,
			@QueryParam("limit") @DefaultValue("1") Integer limit) {
		return statisticsRepository.findTopProductsByQuarter( limit, quarters );
	}

	@GET
	@Path("sales-rollup")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SalesRollupPerQuarterResponseDto> findTopProductsRollup(@QueryParam("limit") @DefaultValue("3") Integer limit) {
		return statisticsRepository.findTopProductsRollup( limit );
	}

	@GET
	@Path("customer-ordering-interval-rollup")
	@Produces(MediaType.APPLICATION_JSON)
	public List<CustomerOrderingIntervalResponseDto> findCustomerOrderingInterval(@QueryParam("customerId") Integer customerId) {
		return statisticsRepository.findCustomerOrderingInterval( customerId );
	}

}
