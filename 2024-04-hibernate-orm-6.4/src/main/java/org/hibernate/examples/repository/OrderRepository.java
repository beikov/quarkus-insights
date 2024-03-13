package org.hibernate.examples.repository;

import java.util.List;

import org.hibernate.annotations.processing.Find;
import org.hibernate.annotations.processing.HQL;
import org.hibernate.examples.entities.Customer_;
import org.hibernate.examples.entities.Order;
import org.hibernate.examples.entities.Order_;
import org.hibernate.examples.representation.OrderOverviewResponseDto;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.JpaRoot;

import jakarta.persistence.EntityManager;

public interface OrderRepository {

    EntityManager entityManager();

    default void persist(Order order) {
        entityManager().persist( order );
    }

    @HQL("from Order o join fetch o.orderLines")
    List<Order> findAllWithOrderLines();

    @Find
    Order findById(long id);

    @HQL("from Order o join fetch o.orderLines where o.id = :id")
    Order findByIdWithOrderLines(long id);

//    @HQL( "from Order o join fetch o.orderLines where o.customer.id = :customerId and o.id = :id" )
    default Order findByCustomerIdAndIdWithOrderLines(long customerId, long id) {
        return entityManager().createQuery(
                new CriteriaDefinition<>( entityManager(), Order.class ) {{
                    var o = from( Order.class );
                    o.fetch( Order_.orderLines );
                    where(
                            o.get( Order_.customer ).get( Customer_.id ).equalTo( customerId ),
                            o.get( Order_.id ).equalTo( id )
                    );
                }}
        ).getSingleResult();
    }

    @HQL( """
        select new org.hibernate.examples.representation.OrderOverviewResponseDto(
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
        """ )
    List<OrderOverviewResponseDto> findAllOrdersForOverview(long customerId);
}
