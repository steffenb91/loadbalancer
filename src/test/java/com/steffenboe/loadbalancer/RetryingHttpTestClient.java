package com.steffenboe.loadbalancer;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.ExecutionException;

class RetryingHttpTestClient {

    private int maxRetries;
    private int currentRetryCount;

    RetryingHttpTestClient(int maxRetries){
        this.maxRetries = maxRetries;
    }

    String sendHttpGetRequest(String address) throws IOException, InterruptedException, ExecutionException {
        currentRetryCount = 0;
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(address))
                .GET()
                .build();
        return sendAndRetry(httpRequest);
    }

    private String sendAndRetry(HttpRequest httpRequest) throws InterruptedException, ExecutionException {
        try {
            return sendAsync(httpRequest);
        } catch (ExecutionException e) {
            if(currentRetryCount > maxRetries){
                currentRetryCount = 0;
                throw new RuntimeException("Retries exhausted.", e); 
            }
            if (e.getCause() instanceof ConnectException) {
                Thread.sleep(10);
                System.out.println("Retrying...");
                currentRetryCount++;
                return sendAndRetry(httpRequest);
            }
            currentRetryCount = 0;
            throw new RuntimeException(e);
        }

    }

    private String sendAsync(HttpRequest httpRequest) throws InterruptedException, ExecutionException {
        return HttpClient.newHttpClient().sendAsync(httpRequest, BodyHandlers.ofString())
                .thenApply(HttpResponse::body).get();
    }
}
