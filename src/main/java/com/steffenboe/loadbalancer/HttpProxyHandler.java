package com.steffenboe.loadbalancer;

public interface HttpProxyHandler {
    
    void handleRequest(HttpRequest request);
}
