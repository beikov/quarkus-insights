package org.hibernate.examples.repository;

import java.util.List;

import org.hibernate.StatelessSession;
import org.hibernate.annotations.processing.Find;
import org.hibernate.examples.entities.CreditCard;

public interface CreditCardRepository {

    StatelessSession session();

    @Find
    List<CreditCard> findAll(long customer$id);

    @Find
    CreditCard findByNumberAndCustomerId(String number, long customer$id);

    default void upsert(CreditCard creditCard) {
        session().upsert( creditCard );
    }
}
