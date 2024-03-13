package org.hibernate.examples;

import org.hibernate.Session;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;


@QuarkusTest
public class OrderTest {

	@Inject
	Session session;

	@AfterEach
	@Transactional
	public void resetData() {
		session.createMutationQuery( "delete Order o where o.customerId = 1" )
				.executeUpdate();
	}

	@Test
	public void testCreateAndReadOrder() {
		var response = given()
				.header( "Customer", "1" )
				.contentType( ContentType.JSON )
				.body(
						"""
								{
								    "orderLines": [
								        {"productVariantId": 1, "quantity": 1},
								        {"productVariantId": 2, "quantity": 1}
								    ]
								}
								"""
				)
				.when().post( "/orders" )
				.then()
				.statusCode( Response.Status.CREATED.getStatusCode() );
		var orderUrl = response.extract().header( "Location" );
		given()
				.header( "Customer", "1" )
				.accept( ContentType.JSON )
				.when().get( orderUrl )
				.then()
				.body( "orderLines[0].netAmount.amount", is( 0.5F ) )
				.body( "orderLines[1].netAmount.amount", is( 1.5F ) );

		given()
				.header( "Customer", "1" )
				.accept( ContentType.JSON )
				.when().get( "/orders" )
				.then()
				.body( "size()", is( 1 ) );
	}

	@Test
	public void testCustomerWithoutOrders() {
		given()
				.header( "Customer", "2" )
				.accept( ContentType.JSON )
				.when().get( "/orders" )
				.then()
				.body( "size()", is( 0 ) );
	}

	@Test
	public void testNonExistingOrder() {
		given()
				.header( "Customer", "1" )
				.accept( ContentType.JSON )
				.when().get( "/orders/{id}", Integer.MAX_VALUE )
				.then()
				.statusCode( Response.Status.NOT_FOUND.getStatusCode() );
	}
}
