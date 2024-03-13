package org.hibernate.examples;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.examples.entities.Customer;
import org.hibernate.examples.entities.ProductVariant;
import org.hibernate.examples.entities.Subscription;
import org.hibernate.examples.entities.SubscriptionItem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;


@QuarkusTest
public class SubscriptionTest {

	@Inject
	Session session;

	@BeforeEach
	@Transactional
	public void setupData() {
		Instant start = Instant.now();
		for ( int i = 0; i < 100; i++ ) {
			var s = new Subscription();
			s.subscriptionStart = start.minus( i, ChronoUnit.DAYS );
			s.subscriptionInterval = Duration.of( 1, ChronoUnit.DAYS );
			s.customer = session.getReference( Customer.class, 1L );
			s.subscriptionItems = List.of(
					new SubscriptionItem( session.getReference( ProductVariant.class, 1L ), BigDecimal.TWO ),
					new SubscriptionItem( session.getReference( ProductVariant.class, 2L ), BigDecimal.ONE )
			);
			session.persist( s );
		}
	}

	@AfterEach
	@Transactional
	public void resetData() {
		session.createMutationQuery( "delete Subscription s where s.customer.id = 1" )
				.executeUpdate();
	}

	@Test
	public void testDefaultPaginateSubscriptions() {
		given()
				.header( "Customer", "1" )
				.when().get( "/subscriptions" )
				.then()
				.body( "size()", is( 10 ) );
	}

	@Test
	public void testPaginateSubscriptionsWithoutSort() {
		given()
				.header( "Customer", "1" )
				.queryParam( "page", 1 )
				.queryParam( "size", 1 )
				.when().get( "/subscriptions" )
				.then()
				.body( "size()", is( 1 ) );
	}

	@Test
	public void testPaginateSubscriptionsWithSort() {
		given()
				.header( "Customer", "1" )
				.queryParam( "page", 1 )
				.queryParam( "size", 1 )
				.queryParam( "sort", "creationDate,desc" )
				.when().get( "/subscriptions" )
				.then()
				.body( "size()", is( 1 ) );
	}
}
