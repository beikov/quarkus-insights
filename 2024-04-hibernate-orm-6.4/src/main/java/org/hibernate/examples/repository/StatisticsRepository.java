package org.hibernate.examples.repository;

import java.util.List;

import org.hibernate.annotations.processing.HQL;
import org.hibernate.examples.representation.CustomerOrderingIntervalResponseDto;
import org.hibernate.examples.representation.SalesRollupPerQuarterResponseDto;
import org.hibernate.examples.representation.TopProductPerQuarterResponseDto;
import org.hibernate.examples.representation.TopProductResponseDto;

import jakarta.persistence.EntityManager;

public interface StatisticsRepository {

    EntityManager entityManager();

    @HQL(
			// Use 3 instead of `CANCELLED` until https://hibernate.atlassian.net/browse/HHH-17782 is fixed
            """
            select new org.hibernate.examples.representation.TopProductResponseDto(
                p.id as id,
                coalesce(sum(l.quantity) filter (where o.status <> 3),0) as amountSold
            )
            from Order o
            join o.orderLines l
            join l.productVariant pv
            right join pv.product p
            group by id
            order by amountSold desc
            fetch first :top rows with ties
            """
    )
    List<TopProductResponseDto> findTopProducts(int top);
    @HQL(
            """
            with lastQuarters as (
                select trunc(current_instant, quarter) as quarterDate, 1 as iteration
                union all
                select trunc(current_instant - (q.iteration) quarter, quarter) as quarterDate, q.iteration + 1 as iteration
                from lastQuarters q
                where q.iteration < :quarters
            )
            select new org.hibernate.examples.representation.TopProductPerQuarterResponseDto(
                extract(year from q.quarterDate) || '/' || extract(quarter from q.quarterDate) as timePeriod,
                sale.productId as id,
                coalesce(sale.amountSold,0) as amountSold
            )
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
            ) sale on sale.timePeriod = q.quarterDate and sale.rank <= :top
            order by timePeriod desc, amountSold desc, id desc
            """
    )
    List<TopProductPerQuarterResponseDto> findTopProductsByQuarter(int top, int quarters);

    @HQL(
            """
            select new org.hibernate.examples.representation.SalesRollupPerQuarterResponseDto(
                extract(year from o.creationDate) || '/' || extract(quarter from o.creationDate) as quarterDate,
                trunc(o.creationDate, month) as monthDate,
                coalesce(sum(l.quantity) filter (where o.status <> CANCELLED),0) as amountSold
            )
            from Order o
            join o.orderLines l
            join l.productVariant pv
            group by rollup(1, 2)
            order by 1 desc nulls last, 2 desc nulls last
            fetch first :top rows with ties
            """
    )
    List<SalesRollupPerQuarterResponseDto> findTopProductsRollup(int top);

    @HQL(
            """
			select new org.hibernate.examples.representation.CustomerOrderingIntervalResponseDto(
				extract(year from o.monthDate) || '/' || extract(quarter from o.monthDate) as quarterDate,
				o.monthDate as monthDate,
				cast(avg(o.orderInterval)*1e9 as Duration)
			)
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
			"""
    )
    List<CustomerOrderingIntervalResponseDto> findCustomerOrderingInterval(int customerId);
}
