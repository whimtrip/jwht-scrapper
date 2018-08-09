package fr.whimtrip.ext.jwhtscrapper.exception;

import fr.whimtrip.ext.jwhtscrapper.intfr.AutomaticScrapperClient;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     Thrown when requests failed too many times in a row for various reasons.
 *     Usually catch or thrown within {@link AutomaticScrapperClient} scope.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class RequestMaxRetriesReachedException extends ScrapperException {
    public RequestMaxRetriesReachedException(Throwable e, String url, int maxRetries, int statusCode) {
        super(
                String.format(
                        "Request at url %s failed %s times with last status code of %s.",
                        url, maxRetries, statusCode
                ),
                e
        );
    }
}
