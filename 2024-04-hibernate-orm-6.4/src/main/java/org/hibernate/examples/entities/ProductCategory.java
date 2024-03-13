package org.hibernate.examples.entities;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_categories")
public class ProductCategory {
	@Id
	@GeneratedValue
	public Long id;

	public String name;
	@ManyToOne(fetch = FetchType.LAZY)
	public ProductCategory parentCategory;
	@OneToMany(mappedBy = "parentCategory")
	public Set<ProductCategory> childCategories;
	@OneToMany(mappedBy = "category")
	public Set<Product> products;
}
