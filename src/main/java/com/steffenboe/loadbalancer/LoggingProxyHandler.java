package com.steffenboe.loadbalancer;

import java.util.logging.Logger;

final class LoggingProxyHandler implements HttpProxyHandler {

    private final String id;
    private final Logger LOG = Logger.getLogger(LoggingProxyHandler.class.getName());

    LoggingProxyHandler(String id) {
        this.id = id;
    }

    @Override
    public String handleRequest(ProxyRequest request) {
        String message = String.format("Server %s received request on %s", id, request.path());
        LOG.info(message);
        return message;
    }

}
