package fr.whimtrip.ext.jwhtscrapper.service.scoped.req;

import fr.whimtrip.ext.jwhtscrapper.intfr.Proxy;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * <p>Some static HTTP related utils methods.</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class RequestUtils {


    /**
     * @param req the HTTP request builder to reset proxy for.
     * @param proxyFinder the proxy finder implementation to find a proxy from.
     * @return the {@link Proxy} found after setting it to the HTTP request builder instance.
     */
    public static Proxy resetProxy(@NotNull final BoundRequestBuilder req, @NotNull final ProxyFinder proxyFinder) {
        Proxy proxy = proxyFinder.findOneRandom();
        req.setProxyServer(proxy.getProxyServer());
        return proxy;
    }


    /**
     * @param proxy the proxy to modify.
     * @param status the new status to attribute it
     * @param proxyFinder the proxyFinder to use to persist the new proxy state
     *                    (if supported by the current implementation of {@link ProxyFinder},
     *                    see {@link ProxyFinder#persistProxy(Proxy)}).
     */
    public static void setProxyStatus(@Nullable final Proxy proxy, @NotNull final Proxy.Status status, @Nullable final ProxyFinder proxyFinder) {
        if(proxy != null && proxyFinder != null) {
            proxy.setStatus(status);
            proxyFinder.persistProxy(proxy);
        }
    }
}
