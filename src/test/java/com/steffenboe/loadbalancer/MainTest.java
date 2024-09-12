package com.steffenboe.loadbalancer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

public class MainTest {

	private final RetryingHttpTestClient client = new RetryingHttpTestClient(10);

	@Test
	public void shouldStartLoggingServer()
			throws URISyntaxException, IOException, InterruptedException, ExecutionException {
		startServer("-p", "8080", "-n", "server");
		client.sendHttpGetRequest("http://localhost:8080/api/test");
	}

	@Test
	public void shouldProxyRequests() throws IOException, InterruptedException, ExecutionException {
		startServer("-p", "8080", "-n", "log1");
		startServer("-p", "8081", "-n", "log2");
		startServer("-p", "8082", "http://localhost:8081/api/test", "http://localhost:8080/api/test");
		client.sendHttpGetRequest("http://localhost:8082/api/test");
		client.sendHttpGetRequest("http://localhost:8082/api/test");
		client.sendHttpGetRequest("http://localhost:8082/api/test");
	}

	private void startServer(String... args) throws InterruptedException {
		Thread.ofVirtual().start(() -> {
			Main.main(args);
		});
	}

}
