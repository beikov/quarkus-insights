package org.hibernate.examples.http;

import java.net.http.HttpClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

public class HttpClientProducer {
	@Produces
	@ApplicationScoped
	public HttpClient createHttpClient() {
		return HttpClient.newHttpClient();
	}

	public void closeHttpClient(@Disposes HttpClient httpClient) {
		httpClient.close();
	}
}
