package com.steffenboe.loadbalancer;

import java.util.List;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.ArrayList;

class RoundRobinProxyHandler implements HttpProxyHandler {

    private List<Proxy> proxies = new ArrayList<>();
    private int nextProxyTarget = 0;
    private static final Logger LOG = Logger.getLogger(RoundRobinProxyHandler.class.getName());

    private volatile List<Proxy> healthyProxies = new ArrayList<>();

    /**
     * @param healthCheckPeriodinMs period to execute healthChecks in, e.g. every
     *                              2000ms
     * @param hosts                 List of backend servers requests are proxied to
     */
    RoundRobinProxyHandler(long healthCheckPeriodinMs, Proxy... hosts) {
        this.proxies = List.of(hosts);
        scheduleHealthChecks(healthCheckPeriodinMs);
    }

    private void scheduleHealthChecks(long period) {
        Thread.ofVirtual().start(() -> {
            while (true) {
                try {
                    executeHealthChecks();
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                    LOG.severe("An error occured, stopping health checks: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void executeHealthChecks() throws InterruptedException {
        LOG.info("Checking backend server's health...");
        for (Proxy proxy : proxies) {
            healthCheckProxy(proxy);
        }
    }

    private void healthCheckProxy(Proxy proxy) {
        Thread.ofVirtual().start(() -> {
            LOG.info("Checking proxy " + proxy);
            try {
                updateHealthyProxies(proxy);
            } catch (IOException | InterruptedException e) {
                LOG.severe("Failed to check proxy health: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        });
    }

    private void updateHealthyProxies(Proxy proxy) throws IOException, InterruptedException {
        if (proxy.healthCheck()) {
            healthyProxies.add(proxy);
            LOG.info("Proxy " + proxy + " is healthy");
        } else {
            healthyProxies.remove(proxy);
            LOG.info("Proxy " + proxy + " is unhealthy");
        }
    }

    @Override
    public String handleRequest(ProxyRequest request) {
        try {
            if (healthyProxies.isEmpty()) {
                return "No healthy proxies";
            }
            String response = healthyProxies.get(nextProxyTarget).receive(request);
            nextProxyTarget = (nextProxyTarget + 1) % healthyProxies.size();
            return response;
        } catch (IOException e) {
            LOG.severe(e.getMessage());
        } catch (InterruptedException e) {
            LOG.severe(e.getMessage());
        }
        return "Failed to handle request";
    }

}
