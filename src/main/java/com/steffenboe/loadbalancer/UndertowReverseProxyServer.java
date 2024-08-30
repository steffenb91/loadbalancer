package com.steffenboe.loadbalancer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.undertow.Undertow;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.util.Headers;

public class UndertowReverseProxyServer {

    private boolean isRunning = false;
    private final List<String> hosts = new ArrayList<>();

    UndertowReverseProxyServer(List<String> hosts) {
        this.hosts.addAll(hosts);
    }

    public void startup(int port) throws URISyntaxException {
        Undertow undertowServer = getUndertowServer();
        Thread.ofVirtual().start(() -> {
            try {
                undertowServer.start();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });
        isRunning = true;
    }

    private Undertow getUndertowServer() throws URISyntaxException {
        HttpProxyHandler reverseProxyHandler = new RoundRobinProxyHandler();
        return undertowReverseProxyServer(reverseProxyHandler);
    }

    private Undertow undertowReverseProxyServer(HttpProxyHandler reverseProxyHandler) {
        return Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(exchange -> {
                    String host = exchange.getRequestHeaders().getFirst(Headers.HOST);
                    String requestPath = exchange.getRequestURI();
                    String queryString = exchange.getQueryString();
                    HttpRequest request = new HttpRequest(exchange.getProtocol() + "://" + host + requestPath
                            + (!queryString.isEmpty() ? "?" + queryString : ""));
                    reverseProxyHandler.handleRequest(request);
                }).build();
    }

    public boolean isRunning() {
        return isRunning;

    }

}
