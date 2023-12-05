package org.hibernate.examples;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;


@QuarkusTest
public class ProductTest {

	@Test
	public void testReadAll() {
		given()
				.accept( ContentType.JSON )
				.when().get( "/products" )
				.then()
				.body( "size()", is( 4 ) )
				.body( "[0].id", is( 1 ) )
				.body( "[1].id", is( 2 ) )
				.body( "[2].id", is( 3 ) )
				.body( "[3].id", is( 4 ) );
	}

	@Test
	public void testCategoryFilterDrink() {
		given()
				.accept( ContentType.JSON )
				.queryParam( "categoryId", 1 )
				.when().get( "/products" )
				.then()
				.body( "size()", is( 3 ) )
				.body( "[0].id", is( 1 ) )
				.body( "[1].id", is( 2 ) )
				.body( "[2].id", is( 3 ) );
	}

	@Test
	public void testCategoryFilterSoftDrink() {
		given()
				.accept( ContentType.JSON )
				.queryParam( "categoryId", 2 )
				.when().get( "/products" )
				.then()
				.body( "size()", is( 2 ) )
				.body( "[0].id", is( 1 ) )
				.body( "[1].id", is( 2 ) );
	}

	@Test
	public void testCategoryFilterAlcohol() {
		given()
				.accept( ContentType.JSON )
				.queryParam( "categoryId", 3 )
				.when().get( "/products" )
				.then()
				.body( "size()", is( 1 ) )
				.body( "[0].id", is( 3 ) );
	}

	@Test
	public void testCategoryFilterSoup() {
		given()
				.accept( ContentType.JSON )
				.queryParam( "categoryId", 4 )
				.when().get( "/products" )
				.then()
				.body( "size()", is( 1 ) )
				.body( "[0].id", is( 4 ) );
	}
}
