package com.steffenboe.loadbalancer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.undertow.Undertow;

@ExtendWith(MockitoExtension.class)
class RoundRobinProxyHandlerTest {

	private Undertow undertowServer;

	@AfterEach
	void tearDown() throws Exception {
		if (undertowServer != null) {
			undertowServer.stop();
		}
	}

	@Test
	void shouldHandleRequest() throws Exception {
		Proxy[] proxies = getThreeMockProxies();
		RoundRobinProxyHandler roundRobinProxyHandler = new RoundRobinProxyHandler(1000,
				proxies);

		Thread.sleep(2000);

		ProxyRequest request = new ProxyRequest("/api/test");
		handleRequestThreeTimes(roundRobinProxyHandler, request);
		verifyEachProxyGotRequest(proxies, request);
	}

	@Test
	void shouldProxyToHealthyServers() throws InterruptedException {
		startUndertowServer(8081, 200);
		RoundRobinProxyHandler roundRobinProxyHandler = new RoundRobinProxyHandler(
				1000, new Proxy("http://localhost:8081/",
						"/health"));

		Thread.sleep(1000);

		String response = roundRobinProxyHandler.handleRequest(new ProxyRequest("/api/test"));
		assertThat(response, is("Ok"));
	}

	@Test
	void shouldNotProxyToUnhealthyServers() throws InterruptedException {
		startUndertowServer(8081, 400);
		RoundRobinProxyHandler roundRobinProxyHandler = new RoundRobinProxyHandler(
				1000, new Proxy("http://localhost:8081/", "/health"));

		Thread.sleep(1000);

		String response = roundRobinProxyHandler.handleRequest(new ProxyRequest("/api/test"));
		assertThat(response, is("No healthy proxies"));
	}

	private void verifyEachProxyGotRequest(Proxy[] proxies, ProxyRequest request)
			throws IOException, InterruptedException {
		for (Proxy proxy : proxies) {
			verify(proxy).receive(request);
		}
	}

	private void handleRequestThreeTimes(RoundRobinProxyHandler roundRobinProxyHandler, ProxyRequest request) {
		IntStream.range(0, 3)
				.forEach(i -> roundRobinProxyHandler.handleRequest(request));
	}

	private Proxy[] getThreeMockProxies() throws IOException, InterruptedException {
		Proxy[] proxies = { mock(Proxy.class), mock(Proxy.class), mock(Proxy.class) };
		for (Proxy proxy : proxies) {
			when(proxy.healthCheck()).thenReturn(true);
		}
		return proxies;
	}

	private void startUndertowServer(int port, int defaultResponse) throws InterruptedException {
		undertowServer = getUndertowServer(port, defaultResponse);
		Thread.ofVirtual().start(() -> {
			startUndertowServer();
		});
		Thread.sleep(100);
	}

	private void startUndertowServer() {
		try {
			undertowServer.start();
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}
	}

	private Undertow getUndertowServer(int port, int responseCode) {
		return Undertow.builder()
				.addHttpListener(port, "localhost")
				.setHandler(exchange -> {
					exchange.setStatusCode(responseCode);
					exchange.getResponseSender().send("Ok");
				}).build();
	}

}
