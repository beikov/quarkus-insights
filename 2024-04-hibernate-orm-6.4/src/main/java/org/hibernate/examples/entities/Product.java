package org.hibernate.examples.entities;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {
	@Id
	@GeneratedValue
	public Long id;

	@Column(nullable = false)
	public String name;
	public ProductType productType;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public ProductCategory category;
	@OneToMany(mappedBy = "product")
	public Set<ProductVariant> variants;
	public Set<String> tags;

}
