package com.steffenboe.loadbalancer;

import java.net.URISyntaxException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        int port = Integer.parseInt(args[0]);
        int option = Integer.parseInt(args[1]);
        if (option == 1) {
            startLoggingServer(port);
        } else {
            startProxyServer(args, port);
        }
    }

    private static void startProxyServer(String[] args, int port) throws URISyntaxException {
        int serverArguments = 2;
        List<Proxy> proxyTargets = new ArrayList<>();
        while (serverArguments < args.length) {
            serverArguments = addProxyFromArgs(args, serverArguments, proxyTargets);
        }
        proxyServer(proxyTargets).startup(port);
    }

    private static UndertowReverseProxyServer proxyServer(List<Proxy> proxyTargets) {
        return new UndertowReverseProxyServer(
                new RoundRobinProxyHandler(30000,
                        proxyTargets.toArray(Proxy[]::new)));
    }

    private static int addProxyFromArgs(String[] args, int serverArguments, List<Proxy> proxyTargets) {
        System.out.println("adding proxy " + args[serverArguments]);
        proxyTargets.add(new Proxy(args[serverArguments], "/health"));
        serverArguments++;
        return serverArguments;
    }

    private static void startLoggingServer(int port) throws URISyntaxException {
        new UndertowReverseProxyServer(new LoggingProxyHandler("1")).startup(port);
    }
}