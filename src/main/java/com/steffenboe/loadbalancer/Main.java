package com.steffenboe.loadbalancer;

import java.net.URISyntaxException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        int port = Integer.parseInt(args[0]);
        int option = Integer.parseInt(args[1]);
        if (option == 1) {
            new UndertowReverseProxyServer(new LoggingProxyHandler("1")).startup(port);
        }
        if (option == 2) {
            int serverArguments = 2;
            List<Proxy> proxyTargets = new ArrayList<>();
            while (serverArguments < args.length) {
                System.out.println("adding proxy " + args[serverArguments]);
                proxyTargets.add(new Proxy(args[serverArguments]));
                serverArguments++;
            }
            new UndertowReverseProxyServer(new RoundRobinProxyHandler(proxyTargets.toArray(Proxy[]::new)))
                    .startup(port);
        }

    }
}