package com.steffenboe.loadbalancer;

public class FileProxyProvider implements ProxyProvider {

    private String file;

    public FileProxyProvider(String file) {
        this.file = file;
    }

    @Override
    public Proxy[] getProxies() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProxies'");
    }

}
