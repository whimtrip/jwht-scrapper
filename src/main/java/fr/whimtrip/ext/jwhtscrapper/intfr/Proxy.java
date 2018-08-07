package fr.whimtrip.ext.jwhtscrapper.intfr;

import fr.whimtrip.ext.jwhtscrapper.enm.Status;
import fr.whimtrip.ext.jwhtscrapper.impl.BasicProxy;
import org.asynchttpclient.proxy.ProxyServer;


/**
 *
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 27/07/18</p>
 *
 * <p>
 *     This interface defines all methods a proxy should have in order
 *     to be usable for our {@link ProxyFinder}.
 * </p>
 *
 * @see BasicProxy Default implementation of this interface.
 * @author Louis-wht
 * @since 1.0.0
 */
public interface Proxy {

    /**
     * Return the id of the proxy. If you do not want to provide a unique id manually,
     * this method will be defaulted with the {@link Object#hashCode()} method.
     * @return the unique id of this proxy.
     */
    default Long getId() {
        return (long) hashCode();
    }


    /**
     *
     * @return the asynchttp lib {@link ProxyServer } object corresponding to this Proxy
     */
    default ProxyServer getProxyServer() {
        return new ProxyServer.Builder(getIpAdress(), getPort()).build();
    }

    /**
     *
     * @return the status of the current proxy.
     * @see Status
     */
    Status getStatus();

    /**
     * @return the ipv4 ip adress of the proxy server
     */
    String getIpAdress();

    /**
     *
     * @return the port to use to connect to the proxy server
     */
    int getPort();



    /**
     * @return the country name or identifier of the proxy location. This is used for
     * debugging purposes only yet. Later on, it might be used to filter out some proxies
     * for example
     */
    default String getCountryName() {
        return "Unknown";
    }

    /**
     * Set a new status to this proxy.
     * @param status the status you want to assign the proxy.
     */
    void setStatus(Status status);

}
