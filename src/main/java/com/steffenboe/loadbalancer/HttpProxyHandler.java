package com.steffenboe.loadbalancer;

public interface HttpProxyHandler {
    
    String handleRequest(ProxyRequest request);
}
