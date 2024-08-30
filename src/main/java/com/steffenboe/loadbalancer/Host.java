package com.steffenboe.loadbalancer;

public class Host {

    private final String adress;
    
    Host(String adress) {
        this.adress = adress;
    }

    public String adress() {
        return adress;
    }
    
    public void receive(HttpRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'receive'");
    }

}
