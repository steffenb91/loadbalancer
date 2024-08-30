package com.steffenboe.loadbalancer;

import java.net.URISyntaxException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        new UndertowReverseProxyServer(List.of("http://localhost:8081")).startup(8080);
    }
}