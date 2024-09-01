package com.steffenboe.loadbalancer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.*;

public class Proxy {

    private final String adress;
    private static final Logger LOG = Logger.getLogger(Proxy.class.getName());


    Proxy(String adress) {
        this.adress = adress;
    }

    public String adress() {
        return adress;
    }

    public String receive(ProxyRequest request) throws IOException, InterruptedException {
        LOG.info("Received request on address " + adress);
        HttpResponse<String> httpResponse = sendHttpRequest(httpClient(), httpGetRequest(request));
        LOG.info("Successfully proxied request to " + adress);
        return httpResponse.body();
    }

    private HttpResponse<String> sendHttpRequest(HttpClient client, HttpRequest httpRequest)
            throws IOException, InterruptedException {
        return client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest httpGetRequest(ProxyRequest request) {
        return HttpRequest.newBuilder()
                .uri(URI.create(adress + request.path()))
                .GET()
                .build();
    }

    private HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

}
