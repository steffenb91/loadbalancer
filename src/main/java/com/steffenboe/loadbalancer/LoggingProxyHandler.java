package com.steffenboe.loadbalancer;

import java.util.logging.Logger;

public final class LoggingProxyHandler implements HttpProxyHandler {

    private final String id;
    private static final Logger LOG = Logger.getLogger(LoggingProxyHandler.class.getName());

    LoggingProxyHandler(String id) {
        this.id = id;
    }

    @Override
    public String handleRequest(ProxyRequest request) {
        String msg = String.format("Server id %s received request: %s", id, request.path());
        LOG.info(msg);
        return msg;
    }

}
