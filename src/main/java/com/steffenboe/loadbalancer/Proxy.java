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
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(adress + request.path()))
                .GET()
                .build();
        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

}
