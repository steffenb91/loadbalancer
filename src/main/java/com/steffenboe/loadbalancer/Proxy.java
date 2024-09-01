package com.steffenboe.loadbalancer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Proxy {

    private final String adress;

    Proxy(String adress) {
        this.adress = adress;
    }

    public String adress() {
        return adress;
    }

    public String receive(ProxyRequest request) throws IOException, InterruptedException {
        HttpClient client = httpClient();
        HttpRequest httpRequest = httpGetRequest(request);
        HttpResponse<String> httpResponse = sendHttpRequest(client, httpRequest);
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
