package org.hibernate.examples.entities;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.PartitionKey;
import org.hibernate.annotations.SourceType;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscriptions")
public class Subscription {
	@Id
	@GeneratedValue
	public Long id;

	@CurrentTimestamp(source = SourceType.VM, event = EventType.INSERT)
	@Column(nullable = false, updatable = false)
	public Instant creationDate;
	@Column(nullable = false)
	public Instant subscriptionStart;
	// Set precision to something greater than zero so that the scale gets registered.
	// This allows the use of the native interval_second type with a scale of 0,
	// which means no support for fractional seconds
	@Column(nullable = false, precision = 18)
	@JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
	public Duration subscriptionInterval;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false, updatable = false)
	public Customer customer;
	// Workaround until https://hibernate.atlassian.net/browse/HHH-17516 is implemented
	@PartitionKey
	@Column(nullable = false, name = "customer_id", insertable = false, updatable = false)
	public Long customerId;

	@ElementCollection
	@CollectionTable(name = "subscription_items")
	public List<SubscriptionItem> subscriptionItems;
}
