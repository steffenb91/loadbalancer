package com.steffenboe.loadbalancer;

import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        new UndertowReverseProxyServer(new LoggingProxyHandler("1")).startup(8080);
    }
}