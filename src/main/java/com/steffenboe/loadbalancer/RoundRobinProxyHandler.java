package com.steffenboe.loadbalancer;

import java.util.List;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.ArrayList;

public class RoundRobinProxyHandler implements HttpProxyHandler {

    private List<Proxy> proxies = new ArrayList<>();
    private int lastProxyTarget = 0;
    private static final Logger LOG = Logger.getLogger(RoundRobinProxyHandler.class.getName());

    public RoundRobinProxyHandler(Proxy... host) {
        this.proxies = List.of(host);
    }

    @Override
    public String handleRequest(ProxyRequest request) {
        try {
            return proxies.get(lastProxyTarget).receive(request);
        } catch (IOException e) {
            LOG.severe(e.getMessage());
        } catch (InterruptedException e) {
            LOG.severe(e.getMessage());
        }
        lastProxyTarget = (lastProxyTarget + 1) % proxies.size();
        return "Failed to handle request";
    }

}
