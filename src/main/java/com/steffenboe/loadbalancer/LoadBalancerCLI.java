package com.steffenboe.loadbalancer;

import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "loadbalancer", mixinStandardHelpOptions = true, version = "0.1", description = "Simple load balancer")
public class LoadBalancerCLI implements Callable<Integer> {

    @Option(names = { "-n", "--names" }, arity = "0..1", description = "Name of the server")
    private String name;
    @Option(names = { "-p", "--port" }, description = "Port to listen on", defaultValue = "8080")
    private int port;
    @Option(names = { "-f", "--file" }, description = "File to watch with backend addresses", defaultValue = "file.txt")
    private String file;
    @Parameters(index = "0", arity = "0..*", description = "Backend server addresses.")
    private String[] proxyAddresses;

    private ProxyProvider proxyProvider;

    private static final Logger LOG = Logger.getLogger(LoadBalancerCLI.class.getName());

    @Override
    public Integer call() throws URISyntaxException, InterruptedException {
        if (name != null && (proxyAddresses == null || proxyAddresses.length == 0)) {
            LOG.info("Starting logging server on port " + port + "...");
            startLoggingServer();
        } else {
            LOG.info("Starting proxy server on port " + port + "...");
            startProxyServer();
        }
        Thread.currentThread().join();
        return 0;
    }

    private void startProxyServer() throws URISyntaxException {
        if(proxyAddresses != null && proxyAddresses.length > 0){
            proxyProvider = new CLIProxyProvider(proxyAddresses);
        } else {
            proxyProvider = new FileProxyProvider(file);
        }
        Proxy[] proxies = proxyProvider.getProxies();
        RoundRobinProxyHandler roundRobinProxyHandler = new RoundRobinProxyHandler(30000, proxies);
        
        new UndertowReverseProxyServer(roundRobinProxyHandler)
                .startup(port);
    }

    private void startLoggingServer() throws URISyntaxException, InterruptedException {
        new UndertowReverseProxyServer(new LoggingProxyHandler(name)).startup(port);

    }

}
