package com.steffenboe.loadbalancer;

class CLIProxyProvider implements ProxyProvider{

    private String[] proxyAddresses;

    CLIProxyProvider(String[] proxyAddresses){
        this.proxyAddresses = proxyAddresses;
    }

    @Override
    public Proxy[] getProxies() {
        Proxy[] proxies = new Proxy[proxyAddresses.length];
        for (int i = 0; i < proxyAddresses.length; i++) {
            proxies[i] = new Proxy(proxyAddresses[i], "/health");
        }
        return proxies;
    }
    
}
