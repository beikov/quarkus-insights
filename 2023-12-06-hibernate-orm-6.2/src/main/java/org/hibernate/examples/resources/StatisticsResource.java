package org.hibernate.examples.resources;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.List;

import org.hibernate.Session;
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
	Session session;

	@GET
	@Path("top-products")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TopProductResponseDto> findTopProducts(@QueryParam("limit") @DefaultValue("3") Integer limit) {
		return session.createQuery(
						"""
								select
								    p.id as id,
								    coalesce(sum(l.quantity) filter (where o.status <> CANCELLED),0) as amountSold
								from Order o
								join o.orderLines l
								join l.productVariant pv
								right join pv.product p
								group by id
								order by amountSold desc
								fetch first :n rows with ties
								""",
						Object[].class
				)
				.setParameter( "n", limit )
				// workaround until https://github.com/quarkusio/quarkus/issues/37518 is fixed
				.setTupleTransformer( (tuple, aliases) -> new TopProductResponseDto(
						(Long) tuple[0],
						(BigDecimal) tuple[1]
				) )
				.getResultList();
	}

	@GET
	@Path("top-products-by-quarter")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TopProductPerQuarterResponseDto> findTopProductsByQuarter(
			@QueryParam("quarters") @DefaultValue("4") Integer quarters,
			@QueryParam("limit") @DefaultValue("1") Integer limit) {
		return session.createQuery(
						"""
								with lastQuarters as (
								    select trunc(current_instant, quarter) as quarterDate, 1 as iteration
								    union all
								    select trunc(current_instant - (q.iteration) quarter, quarter) as quarterDate, q.iteration + 1 as iteration
								    from lastQuarters q
								    where q.iteration < :quarters
								)
								select
								    extract(year from q.quarterDate) || '/' || extract(quarter from q.quarterDate) as timePeriod,
								    sale.productId as id,
								    coalesce(sale.amountSold,0) as amountSold
								from lastQuarters q
								left join (
								    select
								        tmp.timePeriod timePeriod,
								        tmp.productId productId,
								        tmp.amountSold amountSold,
								        row_number() over (partition by tmp.timePeriod order by tmp.timePeriod desc, tmp.amountSold desc) as rank
								    from (
								        select
								            trunc(o.creationDate, quarter) timePeriod,
								            pv.product.id as productId,
								            sum(l.quantity) filter (where o.status <> CANCELLED) as amountSold
								        from Order o
								        join o.orderLines l
								        join l.productVariant pv
								        group by timePeriod, productId
								    ) tmp
								) sale on sale.timePeriod = q.quarterDate and sale.rank <= :n
								order by timePeriod desc, amountSold desc, id desc
								""",
						Object[].class
				)
				.setParameter( "quarters", quarters )
				.setParameter( "n", limit )
				// workaround until https://github.com/quarkusio/quarkus/issues/37518 is fixed
				.setTupleTransformer( (tuple, aliases) -> new TopProductPerQuarterResponseDto(
						(String) tuple[0],
						(Long) tuple[1],
						(BigDecimal) tuple[2]
				) )
				.getResultList();
	}

	@GET
	@Path("sales-rollup")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SalesRollupPerQuarterResponseDto> findTopProductsRollup(@QueryParam("limit") @DefaultValue("3") Integer limit) {
		return session.createQuery(
						"""
								select
								    extract(year from o.creationDate) || '/' || extract(quarter from o.creationDate) as quarterDate,
								    trunc(o.creationDate, month) as monthDate,
								    coalesce(sum(l.quantity) filter (where o.status <> CANCELLED),0) as amountSold
								from Order o
								join o.orderLines l
								join l.productVariant pv
								group by rollup(1, 2)
								order by 1 desc nulls last, 2 desc nulls last
								fetch first :n rows with ties
								""",
						Object[].class
				)
				.setParameter( "n", limit )
				// workaround until https://github.com/quarkusio/quarkus/issues/37518 is fixed
				.setTupleTransformer( (tuple, aliases) -> new SalesRollupPerQuarterResponseDto(
						(String) tuple[0],
						tuple[1] == null
								? null
								: Month.from( ( (Instant) tuple[1] ).atOffset( ZoneOffset.UTC ) ),
						(BigDecimal) tuple[2]
				) )
				.getResultList();
	}

	@GET
	@Path("customer-ordering-interval-rollup")
	@Produces(MediaType.APPLICATION_JSON)
	public List<CustomerOrderingIntervalResponseDto> findCustomerOrderingInterval(@QueryParam("customerId") Integer customerId) {
		return session.createQuery(
						"""
								select
								    extract(year from o.monthDate) || '/' || extract(quarter from o.monthDate) as quarterDate,
								    o.monthDate as monthDate,
								    cast(avg(o.orderInterval)*1e9 as Duration)
								from (
								    select
								        trunc(o.creationDate, month) as monthDate,
								        (o.creationDate
								            - lag(o.creationDate) over (order by o.creationDate)) by second as orderInterval
								    from Order o
								    where o.customerId = :customerId
								) o
								group by rollup(1, 2)
								order by 1 desc nulls last, 2 desc nulls last
								""",
						Object[].class
				)
				.setParameter( "customerId", customerId )
				// workaround until https://github.com/quarkusio/quarkus/issues/37518 is fixed
				.setTupleTransformer( (tuple, aliases) -> new CustomerOrderingIntervalResponseDto(
						(String) tuple[0],
						tuple[1] == null
								? null
								: Month.from( ( (Instant) tuple[1] ).atOffset( ZoneOffset.UTC ) ),
						(Duration) tuple[2]
				) )
				.getResultList();
	}
}
