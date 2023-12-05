package org.hibernate.examples.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {
	@Id
	@GeneratedValue
	public Long id;

	@Column(nullable = false)
	public String firstName;
	@Column(nullable = false)
	public String lastName;
	@Column(nullable = false)
	public BigDecimal[] taxCategoryPercents;
}
