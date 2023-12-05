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


@QuarkusTest
public class OrderProcessingTest {

	@Inject
	Session session;

	@AfterEach
	@Transactional
	public void resetData() {
		session.createMutationQuery( "delete Order o where o.customerId = 1" )
				.executeUpdate();
	}

	@Test
	public void testProcessOrder() {
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
		var orderId = orderUrl.substring( orderUrl.lastIndexOf( '/' ) + 1 );
		given()
				.accept( ContentType.JSON )
				.when().get( "admin/orders/" + orderId )
				.then()
				.statusCode( Response.Status.OK.getStatusCode() );
	}
}
