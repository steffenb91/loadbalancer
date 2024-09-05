package com.steffenboe.loadbalancer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.*;

class Proxy {

    private final String adress;
    private final String healthEndpoint;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static final Logger LOG = Logger.getLogger(Proxy.class.getName());

    Proxy(String adress, String healthEndpoint) {
        this.adress = adress;
        this.healthEndpoint = healthEndpoint;
    }

    /**
     * Forwards the given request to the target adress.
     * 
     * @param request
     * @return the backend server response
     * @throws IOException
     * @throws InterruptedException
     */
    String receive(ProxyRequest request) throws IOException, InterruptedException {
        LOG.info("Received request on address " + adress);
        HttpResponse<String> httpResponse = send(httpGetRequest(request.path()));
        LOG.info("Successfully proxied request to " + adress);
        return httpResponse.body();
    }

    /**
     * Performs a simple health check against the backend adress.
     * 
     * @return true, if response is 200, false otherwise
     * @throws IOException
     * @throws InterruptedException
     */
    boolean healthCheck() throws IOException, InterruptedException {
        return send(httpGetRequest(healthEndpoint)).statusCode() == 200;
    }

    private HttpResponse<String> send(HttpRequest httpRequest)
            throws IOException, InterruptedException {
        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest httpGetRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(adress + path))
                .GET()
                .build();
    }

}
