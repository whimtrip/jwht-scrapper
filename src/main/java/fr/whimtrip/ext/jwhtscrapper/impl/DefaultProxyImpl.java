package fr.whimtrip.ext.jwhtscrapper.impl;

import fr.whimtrip.ext.jwhtscrapper.intfr.Proxy;
import org.asynchttpclient.proxy.ProxyServer;

public class DefaultProxyImpl implements Proxy {

    private Status status = Status.WORKING;

    private String ipAdress;

    private int port;

    public DefaultProxyImpl(String ipAdress, int port) {
        this.ipAdress = ipAdress;
        this.port = port;
    }


    @Override
    public Status getStatus() {

        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String getIpAdress() {
        return ipAdress;
    }

    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public ProxyServer getProxyServer() {
        return new ProxyServer.Builder(ipAdress, port).build();
    }

    public void setPort(int port) {
        this.port = port;
    }
}
