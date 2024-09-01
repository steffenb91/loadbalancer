package com.steffenboe.loadbalancer;

interface HttpProxyHandler {
    
    String handleRequest(ProxyRequest request);
}
