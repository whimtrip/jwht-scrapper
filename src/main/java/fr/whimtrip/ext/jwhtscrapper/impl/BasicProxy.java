package fr.whimtrip.ext.jwhtscrapper.impl;

import fr.whimtrip.ext.jwhtscrapper.intfr.Proxy;
import org.asynchttpclient.proxy.ProxyServer;

public class BasicProxy implements Proxy {

    private Status status = Status.WORKING;

    private String ipAdress;

    private int port;

    /**
     * <p>Public default Proxy implentation</p>
     * @param ipAdress the ip adress of the proxy server.
     * @param port the port to use to connect to the proxy server.
     */
    public BasicProxy(String ipAdress, int port) {
        this.ipAdress = ipAdress;
        this.port = port;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Status getStatus() {

        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIpAdress() {
        return ipAdress;
    }

    /**
     *
     * @param ipAdress the ip adress to set.
     */
    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProxyServer getProxyServer() {
        return new ProxyServer.Builder(ipAdress, port).build();
    }

}
