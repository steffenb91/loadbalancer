package com.steffenboe.loadbalancer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpResponse;

@ExtendWith(MockitoExtension.class)
public class ServerTest {

	private UndertowReverseProxyServer server;
	private ClientAndServer mockServer;

	@BeforeEach
	public void setup() throws InterruptedException {
		this.server = new UndertowReverseProxyServer(List.of("http://localhost:8081"));
		startServer();
		mockServer = startClientAndServer(8081);
	}

	@AfterEach
	public void tearDown() {
		mockServer.stop();
	}

	@Test
	public void shouldStartUp() throws InterruptedException, ClientProtocolException, IOException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            mockServer.when(request().withMethod("GET"))
                    .respond(HttpResponse.response().withBody("backend response").withStatusCode(200));

            HttpGet request = new HttpGet("http://localhost:8080/");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                assertThat(response.getStatusLine().getStatusCode(), is(200));
                HttpEntity entity = response.getEntity();
                assertThat(EntityUtils.toString(entity), is("backend response"));
            }
        }
	}

	private void startServer() throws InterruptedException {
		Thread.ofVirtual().start(() -> {
			try {
				server.startup(8080);
			} catch (URISyntaxException e) {
				fail(e);
			}
		});
		waitForStartUp();
	}

	private void waitForStartUp() throws InterruptedException {
		while (!server.isRunning()) {
			Thread.sleep(100);
		}
	}
}
