package com.steffenboe.loadbalancer;

import java.io.IOException;
import java.util.List;

class RoundRobinProxyHandler implements HttpProxyHandler {

    private final List<Proxy> proxies;
    private final HealthCheck healthCheck;

    private int nextProxyTarget = 0;

    /**
     * @param healthCheckPeriodinMs period to execute healthChecks in, e.g. every
     *                              2000ms
     * @param hosts                 List of backend servers requests are proxied to
     * @throws InterruptedException
     */
    RoundRobinProxyHandler(long healthCheckPeriodinMs, Proxy... hosts) {
        this.proxies = List.of(hosts);
        healthCheck = new HealthCheck(proxies, healthCheckPeriodinMs);
        healthCheck.schedule();
    }

    @Override
    public String handleRequest(ProxyRequest request) {
        try {
            return dispatchToHealthyProxy(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String dispatchToHealthyProxy(ProxyRequest request) throws IOException, InterruptedException {
        if (healthCheck.healthyProxies().isEmpty()) {
            throw new RuntimeException("No healthy proxies");
        }
        return requestNextHealthyProxy(request);
    }

    private String requestNextHealthyProxy(ProxyRequest request) throws IOException, InterruptedException {
        String response = healthCheck.healthyProxies().get(nextProxyTarget).receive(request);
        nextProxyTarget = (nextProxyTarget + 1) % healthCheck.healthyProxies().size();
        return response;
    }

}
