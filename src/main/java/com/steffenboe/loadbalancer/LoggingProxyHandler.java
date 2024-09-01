package com.steffenboe.loadbalancer;

final class LoggingProxyHandler implements HttpProxyHandler {

    private final String id;

    LoggingProxyHandler(String id) {
        this.id = id;
    }

    @Override
    public String handleRequest(ProxyRequest request) {
        return String.format("Server id %s received request: %s", id, request.path());
    }

}
