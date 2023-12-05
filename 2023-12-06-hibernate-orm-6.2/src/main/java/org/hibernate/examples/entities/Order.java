package org.hibernate.examples.entities;

import java.net.InetAddress;
import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.annotations.PartitionKey;
import org.hibernate.annotations.SourceType;
import org.hibernate.generator.EventType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
	@Id
	@GeneratedValue
	public Long id;

	@CurrentTimestamp(source = SourceType.VM, event = EventType.INSERT)
	@Column(nullable = false, updatable = false)
	public Instant creationDate;
	@Column(nullable = false, updatable = false)
	public InetAddress createdFromAddress;
	@Column(nullable = false)
	public OrderStatus status = OrderStatus.OPEN;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false, updatable = false)
	public Customer customer;
	// Workaround until https://hibernate.atlassian.net/browse/HHH-17516 is implemented
	@PartitionKey
	@Column(nullable = false, name = "customer_id", insertable = false, updatable = false)
	public Long customerId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false)
	public Subscription subscription;

	@ElementCollection
	@OrderColumn(name = "line_number")
	@CollectionTable(name = "order_lines", joinColumns = {
			@JoinColumn(name = "order_id", referencedColumnName = "id"),
			@JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
	})
	public List<OrderLine> orderLines;
}
