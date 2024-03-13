package org.hibernate.examples.repository;

import org.hibernate.Session;
import org.hibernate.annotations.processing.Find;
import org.hibernate.examples.entities.Customer;

public interface CustomerRepository {

    Session session();

    @Find
    Customer findById(long id);

    default void remove(long id) {
        session().remove( session().getReference( Customer.class, id ) );
    }

}
