/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.annotation;

import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import fr.whimtrip.ext.jwhtscrapper.service.AutomaticScrapperManagerBuilder;
import fr.whimtrip.ext.jwhtscrapper.service.base.AutomaticScrapperManager;
import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.HttpWithProxyManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.req.HttpConnectHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     This annotation should be added to {@link RequestsConfig#proxyConfig()}
 *     and will define how proxying should be handled. If {@link #useProxy()}
 *     returns true, you <strong>MUST</strong> provide your custom {@link ProxyFinder}
 *     implementation using {@link AutomaticScrapperManagerBuilder#setProxyFinder(ProxyFinder)}
 *     when creating your {@link AutomaticScrapperManager}.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface ProxyConfig {

    int PROXY_CHANGE_RATE = 5;

    /**
     * @return a boolean indicating if proxies should be used or not. If it
     *         returns true, you <strong>MUST</strong> provide your custom
     *         {@link ProxyFinder} implementation using
     *         {@link AutomaticScrapperManagerBuilder#setProxyFinder(ProxyFinder)}
     *         when creating your {@link AutomaticScrapperManager}.
     */
    boolean useProxy() default false;


    /**
     * @return wether TCP Connect should be used before making the actual
     *         HTTP request. Currently handled through the default implementation
     *         of {@link HttpManagerClient} by {@link HttpConnectHandler}.
     */
    boolean connectToProxyBeforeRequest() default false;

    /**
     * @deprecated not supported by default implementation {@link HttpWithProxyManagerClient}.
     * @return the number of requests to perform before changing the Proxy.
     *         currently not supported by {@link HttpWithProxyManagerClient}
     *         because proxies change at almost every request try as they
     *         tend to be very unstable.
     */
    @Deprecated
    int proxyChangeRate() default PROXY_CHANGE_RATE;

}
