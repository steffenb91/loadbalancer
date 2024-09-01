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
                    String requestPath = exchange.getRequestPath();
                    String queryString = exchange.getQueryString();
                    String proxyUri = requestPath
                            + (!queryString.isEmpty() ? "?" + queryString : "");
                    ProxyRequest request = new ProxyRequest(proxyUri);
                    String responseString = httpProxyHandler.handleRequest(request); 
                    exchange.getResponseHeaders().put(io.undertow.util.Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(responseString); 
                }).build();
    }

    public boolean isRunning() {
        return isRunning;

    }

}
