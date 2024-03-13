package org.hibernate.examples.repository;

import java.util.List;

import org.hibernate.StatelessSession;
import org.hibernate.annotations.processing.Find;
import org.hibernate.annotations.processing.HQL;
import org.hibernate.examples.entities.Customer;
import org.hibernate.examples.entities.ProductVariant;
import org.hibernate.examples.entities.Subscription;
import org.hibernate.query.Order;
import org.hibernate.query.Page;

import jakarta.persistence.EntityManager;

public interface SubscriptionRepository {

    EntityManager entityManager();

    @Find
    Subscription findById(long id);

    @HQL( "from Subscription s where s.customer.id = :customerId" )
    List<Subscription> findAll(long customerId, Page page, List<Order<Subscription>> orders);

}
