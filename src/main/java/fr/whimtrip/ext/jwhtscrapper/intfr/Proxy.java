package fr.whimtrip.ext.jwhtscrapper.intfr;

import org.asynchttpclient.proxy.ProxyServer;

public interface Proxy {


    enum Status {

        /** Working Proxy **/
        WORKING,
        /**
         * Shouldn't be used as of now but a process to make a proxy
         * change from Frozen to WORKING might be imagined
         **/
        FROZEN,
        /** Not to be used anymore **/
        BANNED;



    }
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
     *
     * @return the asynchttp lib {@link ProxyServer } object corresponding to this Proxy
     */
    ProxyServer getProxyServer();


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
