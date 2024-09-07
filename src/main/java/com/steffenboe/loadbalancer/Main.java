package com.steffenboe.loadbalancer;

import picocli.CommandLine;

public class Main {

    public static void main(String[] args) {
        new CommandLine(new LoadBalancerCLI()).execute(args);
    }

    
}
