package com.steffenboe.loadbalancer;

import java.util.List;
import java.util.ArrayList;

public class RoundRobinProxyHandler implements HttpProxyHandler {

    private List<Host> hosts = new ArrayList<>();
    private int lastProxyTarget = 0;

    public RoundRobinProxyHandler(Host... host) {
        this.hosts = List.of(host);
    }

    @Override
    public void handleRequest(HttpRequest request) {
        hosts.get(lastProxyTarget).receive(request);
        lastProxyTarget = (lastProxyTarget + 1) % hosts.size();
    }

}
