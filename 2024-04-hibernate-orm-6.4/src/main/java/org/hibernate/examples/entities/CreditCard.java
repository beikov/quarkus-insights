package org.hibernate.examples.entities;

import java.time.Instant;

import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.annotations.PartitionKey;
import org.hibernate.annotations.SourceType;
import org.hibernate.generator.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "credit_cards")
public class CreditCard {

	@Id
	@Column(length = 19)
	public String number;
	public String holderName;
	@Column(length = 3)
	public String csc;

	// Do not use updatable=false columns along with upsert until https://hibernate.atlassian.net/browse/HHH-17786 is fixed
//	@CurrentTimestamp(source = SourceType.VM, event = EventType.INSERT)
//	@Column(nullable = false, updatable = false)
//	public Instant creationDate;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false)
	// Do not use updatable=false columns along with upsert until https://hibernate.atlassian.net/browse/HHH-17786 is fixed
//	@JoinColumn(nullable = false, updatable = false)
	public Customer customer;
	// Can't use partition key until https://hibernate.atlassian.net/browse/HHH-17785 is fixed
//	// Workaround until https://hibernate.atlassian.net/browse/HHH-17516 is implemented
//	@PartitionKey
//	@Column(nullable = false, name = "customer_id", insertable = false, updatable = false)
//	public Long customerId;
}
