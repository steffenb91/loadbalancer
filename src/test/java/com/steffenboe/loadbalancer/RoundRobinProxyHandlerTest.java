package com.steffenboe.loadbalancer;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import java.net.URISyntaxException;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import io.undertow.Undertow;

@ExtendWith(MockitoExtension.class)
class RoundRobinProxyHandlerTest {

	@Test
	void shouldHandleRequest() throws Exception {
		ProxyRequest request = new ProxyRequest("/api/test");
		Proxy host1 = mock(Proxy.class);
		Proxy host2 = mock(Proxy.class);
		Proxy host3 = mock(Proxy.class);

		RoundRobinProxyHandler roundRobinProxyHandler = new RoundRobinProxyHandler(host1, host2, host3);

		roundRobinProxyHandler.handleRequest(request);
		roundRobinProxyHandler.handleRequest(request);
		roundRobinProxyHandler.handleRequest(request);
		roundRobinProxyHandler.handleRequest(request);

		InOrder inOrder = inOrder(host1, host2, host3, host1);

		inOrder.verify(host1).receive(request);
		inOrder.verify(host2).receive(request);
		inOrder.verify(host3).receive(request);
		inOrder.verify(host1).receive(request);
	}

	@Test
	void shouldProxyToHealthyServers() throws InterruptedException {
		startUndertowServer(8081, 200);
		RoundRobinProxyHandler roundRobinProxyHandler = new RoundRobinProxyHandler(new Proxy("http://localhost:8081/"));
		Thread.sleep(1000);
		String response = roundRobinProxyHandler.handleRequest(new ProxyRequest("/api/test"));
		assertThat(response, is("Ok"));
	}

	@Test
	void shouldNotProxyToUnhealthyServers() throws InterruptedException {
		startUndertowServer(8081, 400);
		RoundRobinProxyHandler roundRobinProxyHandler = new RoundRobinProxyHandler(new Proxy("http://localhost:8081/"));
		Thread.sleep(1000);
		String response = roundRobinProxyHandler.handleRequest(new ProxyRequest("/api/test"));
		assertThat(response, is("No healthy proxies"));
	}

	private void startUndertowServer(int port, int responseCode) throws InterruptedException {
		Undertow testServer = Undertow.builder()
				.addHttpListener(port, "localhost")
				.setHandler(exchange -> {
					exchange.setStatusCode(responseCode);
				}).build();
		Thread.ofVirtual().start(() -> {
			try {
				testServer.start();
			} catch (Exception e) {
				Thread.currentThread().interrupt();
			}
		});
		Thread.sleep(1000);
	}

	private void startServer(UndertowReverseProxyServer server, int port) throws InterruptedException {
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
