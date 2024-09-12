package com.steffenboe.loadbalancer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class HealthCheck {

    private long periodInMs;
    private List<Proxy> proxies;
    private volatile List<Proxy> healthyProxies;
    private static final Logger LOG = Logger.getLogger(HealthCheck.class.getName());

    HealthCheck(List<Proxy> proxies, long periodInMs){
        this.proxies = proxies;
        this.healthyProxies = new ArrayList<>(proxies);
        this.periodInMs = periodInMs;
    }

    void schedule(){
        Thread.ofVirtual().start(() -> {
            performPeriodicHealthChecks(periodInMs);
        });
    }

    private void performPeriodicHealthChecks(long period) {
        while (true) {
            try {
                executeHealthChecks();
                Thread.sleep(period);
            } catch (InterruptedException e) {
                LOG.severe("An error occured, stopping health checks: " + e.getMessage());
            }
        }
    }

    private void executeHealthChecks() {
        LOG.info("Checking backend servers health...");
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
                healthyProxies.remove(proxy);
                LOG.severe("Failed to check proxy health: " + e);
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

    List<Proxy> healthyProxies() {
        return new ArrayList<>(healthyProxies);
    }
}
