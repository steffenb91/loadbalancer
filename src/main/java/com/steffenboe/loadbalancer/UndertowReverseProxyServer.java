package com.steffenboe.loadbalancer;

import java.net.URISyntaxException;

import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;

class UndertowReverseProxyServer {

    private boolean isRunning = false;
    private HttpProxyHandler httpProxyHandler;
    private Undertow undertowServer;

    UndertowReverseProxyServer(HttpProxyHandler httpProxyHandler) {
        this.httpProxyHandler = httpProxyHandler;
    }

    void startup(int port) throws URISyntaxException {
        undertowServer = undertowReverseProxyServer(port);
        undertowServer.start();
        isRunning = true;
    }

    private Undertow undertowReverseProxyServer(int port) {
        return Undertow.builder()
                .addHttpListener(port, "localhost")
                .setHandler(exchange -> {
                    respond(exchange);
                }).build();
    }

    private void respond(HttpServerExchange exchange) {
        String requestPath = exchange.getRequestPath();
        String queryString = exchange.getQueryString();
        String proxyUri = getFullProxyUri(requestPath, queryString);
        ProxyRequest request = new ProxyRequest(proxyUri);

        String responseString = httpProxyHandler.handleRequest(request);

        sendResponse(exchange, responseString);
    }

    private void sendResponse(HttpServerExchange exchange, String responseString) {
        exchange.getResponseHeaders().put(io.undertow.util.Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send(responseString);
    }

    private String getFullProxyUri(String requestPath, String queryString) {
        return requestPath
                + (!queryString.isEmpty() ? "?" + queryString : "");
    }

    synchronized boolean isRunning() {
        return isRunning;

    }

    void stop() {
        undertowServer.stop();
        isRunning = false;
    }

}
