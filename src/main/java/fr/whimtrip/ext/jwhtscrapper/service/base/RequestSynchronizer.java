package fr.whimtrip.ext.jwhtscrapper.service.base;

import fr.whimtrip.ext.jwhtscrapper.intfr.HttpMetrics;
import fr.whimtrip.ext.jwhtscrapper.service.holder.HttpManagerConfig;
import fr.whimtrip.ext.jwhtscrapper.service.holder.StatusRange;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * <p>
 *     Any request synchronizer implementing class will help every concurrent
 *     access to the underlying httpclient to be synchronyzed on the same object
 *     instance to ensure no two requests are performed without the required
 *     {@link HttpManagerConfig#getAwaitBetweenRequests()} wait between those
 *     two requests. Additionnally, this should help monitoring the last proxy
 *     change or the count of failed requests / HTTP status stats.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface RequestSynchronizer {


    /**
     * @return the milliseconds since last request has started.
     */
    Long getLastRequest();

    /**
     * @return the number of requests since last proxy change happend.
     */
    int getLastProxyChange();

    /**
     * Should increment in a synchronized way the inner {@code lastProxyChange}.
     */
    void incrementLastProxyChange();

    /**
     * @param url the url to use for logging purpose when awaiting for the
     *            next request to perform.
     */
    void checkAwaitBetweenRequest(String url);

    /**
     * @param httpStatus the httpStatus of the resulting HTTP response to log.
     *                   <strong>
     *                       Timeout exceptions should be logged with a
     *                       {@link StatusRange#TIMEOUT_STATUS_CODE} HTTP
     *                       status code.
     *                   </strong>
     *                   <strong>
     *                       Unknown exceptions should be logged with a
     *                       {@link StatusRange#UNKNOWN_EXCEPTION_STATUS_CODE}
     *                       HTTP status code.
     *                   </strong>
     * @param newScrap wether it's a first time try of this current request or
     *                 if it's a retry. It will be used for statistical purposes.
     */
    void logHttpStatus(int httpStatus, boolean newScrap);

    /**
     * @see HttpMetrics
     * @return the corresponding {@link HttpMetrics} of the current scrapping operation.
     */
    HttpMetrics getHttpMetrics();
}
