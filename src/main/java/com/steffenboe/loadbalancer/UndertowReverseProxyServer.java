package com.steffenboe.loadbalancer;

import java.net.URISyntaxException;

import io.undertow.Undertow;

public class UndertowReverseProxyServer {

    private boolean isRunning = false;
    private HttpProxyHandler httpProxyHandler;

    UndertowReverseProxyServer(HttpProxyHandler httpProxyHandler) {
        this.httpProxyHandler = httpProxyHandler;
    }

    public void startup(int port) throws URISyntaxException {
        Undertow undertowServer = undertowReverseProxyServer(port);
        Thread.ofVirtual().start(() -> {
            try {
                undertowServer.start();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });
        isRunning = true;
    }

    private Undertow undertowReverseProxyServer(int port) {
        return Undertow.builder()
                .addHttpListener(port, "localhost")
                .setHandler(exchange -> {
                    String host = exchange.getHostAndPort();
                    System.out.println(host);
                    String requestPath = exchange.getRequestPath();
                    System.out.println(requestPath);
                    String queryString = exchange.getQueryString();
                    String proxyUri = requestPath
                            + (!queryString.isEmpty() ? "?" + queryString : "");
                    System.out.println(proxyUri);
                    ProxyRequest request = new ProxyRequest(proxyUri);
                    String responseString = httpProxyHandler.handleRequest(request); // Get response from handler
                    exchange.getResponseHeaders().put(io.undertow.util.Headers.CONTENT_TYPE, "text/plain"); // Set content type
                    exchange.getResponseSender().send(responseString); // Send the response
                }).build();
    }

    public boolean isRunning() {
        return isRunning;

    }

}
