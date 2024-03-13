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
import static org.hamcrest.CoreMatchers.notNullValue;


@QuarkusTest
public class CreditCardTest {

	@Inject
	Session session;

	@AfterEach
	@Transactional
	public void resetData() {
		session.createMutationQuery( "delete CreditCard c where c.customer.id = 1" )
				.executeUpdate();
	}

	@Test
	public void testUpsertAndReadCreditCard() {
		given()
				.header( "Customer", "1" )
				.contentType( ContentType.JSON )
				.body(
						"""
								{
								    "holderName": "Customer 1",
									"csc": "999"
								}
								"""
				)
				.when().put( "/credit-cards/1234567809876543" )
				.then()
				.statusCode( Response.Status.OK.getStatusCode() );
		given()
				.header( "Customer", "1" )
				.accept( ContentType.JSON )
				.when().get( "/credit-cards/1234567809876543" )
				.then()
				.body( "holderName", is( "Customer 1" ) );

		given()
				.header( "Customer", "1" )
				.accept( ContentType.JSON )
				.when().get( "/credit-cards" )
				.then()
				.body( "size()", is( 1 ) );
	}

	@Test
	public void testCustomerWithoutCreditCards() {
		given()
				.header( "Customer", "2" )
				.accept( ContentType.JSON )
				.when().get( "/credit-cards" )
				.then()
				.body( "size()", is( 0 ) );
	}

	@Test
	public void testNonExistingCreditCard() {
		given()
				.header( "Customer", "1" )
				.accept( ContentType.JSON )
				.when().get( "/credit-cards/{id}", Integer.MAX_VALUE )
				.then()
				.statusCode( Response.Status.NOT_FOUND.getStatusCode() );
	}
}
