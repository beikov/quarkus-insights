package org.hibernate.examples.resources;

import java.util.TimeZone;

import jakarta.ws.rs.core.Application;

public class InsightsApplication extends Application {

	public InsightsApplication() {
		super();
		TimeZone.setDefault( TimeZone.getTimeZone( "UTC" ) );
	}
}