package com.steffenboe.loadbalancer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileProxyProvider implements ProxyProvider {

    private String file;

    public FileProxyProvider(String file) {
        this.file = file;
    }

    @Override
    public Proxy[] getProxies() {
        
        try {
            List<Proxy> proxies = Files.readAllLines(Paths.get(file)).stream().map(line -> {
                System.out.println("Adding " + line + " to proxy list");
                return new Proxy(line, "/health");
            }).collect(Collectors.toList());
            return proxies.toArray(new Proxy[proxies.size()]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Proxy[] {};
    }

}
