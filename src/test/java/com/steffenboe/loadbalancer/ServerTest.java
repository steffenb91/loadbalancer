package com.steffenboe.loadbalancer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ServerTest {

	@Test
	void shouldProxyRequest() throws IOException, InterruptedException {
		UndertowReverseProxyServer proxyServer = undertowReverseProxyServer();
		start(proxyServer, 8080);

		UndertowReverseProxyServer targetServer = undertowLoggingServer();
		start(targetServer, 8081);

		HttpResponse<String> response = requestProxyServer();
		assertThat(response.body(), is("Server id 1 received request: /api/test"));
	}

	private HttpResponse<String> requestProxyServer() throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest httpRequest = getHttpRequest();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		return response;
	}

	private HttpRequest getHttpRequest() {
		return HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:8080/api/test"))
				.GET()
				.build();
	}

	private UndertowReverseProxyServer undertowLoggingServer() {
		return new UndertowReverseProxyServer(new LoggingProxyHandler("1"));
	}

	private UndertowReverseProxyServer undertowReverseProxyServer() {
		return new UndertowReverseProxyServer(
				new RoundRobinProxyHandler(500,
						new Proxy("http://localhost:8081", "/health")));
	}

	private void start(UndertowReverseProxyServer server, int port) throws InterruptedException {
		Thread.ofVirtual().start(() -> {
			try {
				server.startup(port);
			} catch (URISyntaxException e) {
				fail(e);
			}
		});
		waitForStartUp(server);
	}

	private void waitForStartUp(UndertowReverseProxyServer server) throws InterruptedException {
		while (!server.isRunning()) {
			Thread.sleep(100);
		}
	}
}
