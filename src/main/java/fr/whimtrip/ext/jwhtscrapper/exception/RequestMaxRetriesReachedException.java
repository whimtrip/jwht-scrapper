package fr.whimtrip.ext.jwhtscrapper.exception;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
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
