package com.steffenboe.loadbalancer;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

/**
 * Just for "real" testing purposes.... Check logs to see system behavior in
 * action.
 */
public class MainTest {

	@Test
	public void shouldStartLoggingServer() throws URISyntaxException, IOException, InterruptedException {
		startServer("8080", "1");
		sendHttpGetRequest("http://localhost:8080/api/test");
	}

	@Test
	public void shouldProxyRequests() throws IOException, InterruptedException {
		startServer("8080", "1");
		startServer("8081", "1");
		startServer("8082", "2", "http://localhost:8081/api/test", "http://localhost:8080/api/test");
		sendHttpGetRequest("http://localhost:8082/api/test");
		sendHttpGetRequest("http://localhost:8082/api/test");
		sendHttpGetRequest("http://localhost:8082/api/test");
	}

	private void sendHttpGetRequest(String address) throws IOException, InterruptedException {
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(URI.create(address))
				.GET()
				.build();
		HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
	}

	private void startServer(String... args) throws InterruptedException {
		Thread.ofVirtual().start(() -> {
			try {
				Main.main(args);
			} catch (URISyntaxException e) {
				fail(e);
			}
		});
		Thread.sleep(1000);
	}

}
