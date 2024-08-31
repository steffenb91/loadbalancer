package com.steffenboe.loadbalancer;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RoundRobinProxyHandlerTest {

	@Test
	public void shouldHandleRequest() throws Exception {
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
}
