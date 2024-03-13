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
public class CustomerTest {

	@Inject
	Session session;

	@AfterEach
	@Transactional
	public void resetData() {
		session.createNativeMutationQuery( "update customers c set deleted = :deleted where c.id = 1" )
				.setParameter( "deleted", false )
				.executeUpdate();
	}

	@Test
	public void testDeleteCustomer() {
		given()
				.header( "Customer", "1" )
				.accept( ContentType.JSON )
				.when().get( "/profile" )
				.then()
				.body( "firstName", is( "Tony" ) )
				.body( "lastName", is( "Stark" ) );

		given()
				.header( "Customer", "1" )
				.contentType( ContentType.JSON )
				.when().delete( "/profile" )
				.then()
				.statusCode( Response.Status.OK.getStatusCode() );

		given()
				.header( "Customer", "1" )
				.accept( ContentType.JSON )
				.when().get( "/profile" )
				.then()
				.statusCode( Response.Status.NOT_FOUND.getStatusCode() );
	}

	@Test
	public void testDeleteNonExistingCustomer() {
		given()
				.header( "Customer", "100" )
				.when().delete( "/profile" )
				.then()
				.statusCode( Response.Status.NOT_FOUND.getStatusCode() );
	}
}
