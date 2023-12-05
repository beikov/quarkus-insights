package org.hibernate.examples.entities;

import org.hibernate.annotations.Struct;

import jakarta.persistence.Embeddable;

@Embeddable
@Struct(name = "product_variant_details", attributes = { "description", "color" })
public record ProductVariantDetails(String description, String color) {
}
