package fr.whimtrip.ext.jwhtscrapper.exception;

import fr.whimtrip.ext.jwhtscrapper.annotation.ProxyConfig;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import fr.whimtrip.ext.jwhtscrapper.service.AutomaticScrapperManagerBuilder;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 08/08/18</p>
 *
 * <p>
 *     Thrown when no {@link ProxyFinder} was provided when constructing the
 *     scrapper {@link AutomaticScrapperManagerBuilder#setProxyFinder(ProxyFinder)}
 *     while {@link ProxyConfig#useProxy()} returns {@code true}.
 * </p>
 * @author Louis-wht
 * @since 1.0.0
 */
public class NoProxyFinderProvidedException extends ScrapperException {

    public NoProxyFinderProvidedException() {
        super(
                String.format(
                    "No %s instance was submitted through %s method " +
                    "\"setProxyFinder\" while %s \"useProxy\" is enabled.",
                    ProxyFinder.class,
                    AutomaticScrapperManagerBuilder.class,
                    ProxyConfig.class
                )
        );
    }
}
