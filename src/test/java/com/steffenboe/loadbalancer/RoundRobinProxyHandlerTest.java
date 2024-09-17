package com.steffenboe.loadbalancer;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoundRobinProxyHandlerTest {

	@Test
	void shouldHandleRequest() throws Exception {
		Proxy[] proxies = getThreeMockProxies();
		RoundRobinProxyHandler roundRobinProxyHandler = new RoundRobinProxyHandler(1000,
				proxies);

		ProxyRequest request = new ProxyRequest("/api/test");
		handleRequestThreeTimes(roundRobinProxyHandler, request);
		verifyEachProxyGotRequest(proxies, request);
	}

	@Test
	void shouldProxyToHealthyServers() throws InterruptedException, IOException {
		Proxy healthyProxy = mock(Proxy.class);
		when(healthyProxy.healthCheck()).thenReturn(true);
		ProxyRequest request = new ProxyRequest("/api/test");
		when(healthyProxy.receive(request)).thenReturn("Ok");

		Proxy unhealthyProxy = mock(Proxy.class);
		when(unhealthyProxy.healthCheck()).thenReturn(false);

		RoundRobinProxyHandler roundRobinProxyHandler = new RoundRobinProxyHandler(
				1, healthyProxy, unhealthyProxy);

		handleRequestThreeTimes(roundRobinProxyHandler, request);

		verify(healthyProxy, times(3)).receive(request);
		verify(unhealthyProxy, never()).receive(request);

	}

	@Test
	void shouldUpdateProxies() throws InterruptedException, IOException {
		RoundRobinProxyHandler roundRobinProxyHandler = new RoundRobinProxyHandler(1000,
				mock(Proxy.class));

		roundRobinProxyHandler.handleRequest(new ProxyRequest("/api/test"));

		Proxy[] updatedProxies = getThreeMockProxies();
		roundRobinProxyHandler = roundRobinProxyHandler.updateProxies(updatedProxies);

		ProxyRequest request = new ProxyRequest("api/test");
		handleRequestThreeTimes(roundRobinProxyHandler, request);
		verifyEachProxyGotRequest(updatedProxies, request);
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
			lenient().when(proxy.healthCheck()).thenReturn(true);
		}
		return proxies;
	}

}
