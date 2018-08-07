package fr.whimtrip.ext.jwhtscrapper.intfr;

import org.jetbrains.annotations.NotNull;

/**
 *
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 27/07/18</p>
 *
 * <p>
 *   Any class implementing this one should be able to retrieve and
 *   edit a Proxy list, ideally from some persisting memory technology
 *   such as an SQL database. In the case of using a classical ORM such
 *   as Hibernate, the ProxyFinder implementation might be a service wrapping
 *   the real Proxy Repository for example.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface ProxyFinder {

    /**
     * Should return a proxy given its ipAdress. As this ip adress must
     * have been found earlier through this {@link ProxyFinder}, this should
     * not return a null value.
     * @param ipAdress the ip adress of the {@link Proxy} to search for.
     * @return a Proxy whose ip adress is the one submitted
     */
    @NotNull
    Proxy findOneByIp(@NotNull final String ipAdress);


    /**
     * @return one random proxy. The more proxies you will have, the better.
     */
    @NotNull
    Proxy findOneRandom();

    /**
     * If this proxy Finder supports status persisting, then this method should
     * be overriden to provide an object persisting implementation.
     * @param proxy the proxy to persist.
     */
    void persistProxy(Proxy proxy);
}
