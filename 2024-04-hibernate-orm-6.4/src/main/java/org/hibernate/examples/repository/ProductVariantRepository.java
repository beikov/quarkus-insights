package org.hibernate.examples.repository;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.annotations.processing.Find;
import org.hibernate.examples.entities.ProductVariant;

public interface ProductVariantRepository {

    Session session();

    @Find
    ProductVariant findById(long id);

    default List<ProductVariant> findAllById(Long... ids) {
        return session().byMultipleIds( ProductVariant.class )
                .multiLoad( ids );
    }
}
