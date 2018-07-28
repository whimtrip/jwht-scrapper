package fr.whimtrip.ext.jwhtscrapper.service.base;

import fr.whimtrip.ext.jwhtscrapper.exception.RequestTimeoutException;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import fr.whimtrip.ext.jwhtscrapper.service.holder.HttpManagerConfig;
import org.asynchttpclient.BoundRequestBuilder;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * <p>
 *     An implementing class of this interface should be able to deal
 *     with all of the requirements set up by {@link HttpManagerConfig}.
 *     Please see each method javadoc in order to understand and apply
 *     correctly all the requirements.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface HttpManagerClient {


    /**
     * <p>
     *     This method should prepare a GET request while returning an
     *     {@link BoundRequestBuilder}. The elements to be prepared by
     *     any implementing class are the following ;
     * </p>
     * <ul>
     *     <li>The eventual proxy to use if proxies are enabled</li>
     *     <li>The default headers to use</li>
     *     <li>The default cookies to use</li>
     *     <li>The rotating User Agent header if enabled</li>
     *     <li>The url to scrap the request to</li>
     * </ul>
     * @param url the url to prepare
     * @return the prepared {@link BoundRequestBuilder} ready to be executed.
     */
    BoundRequestBuilder prepareGet(String url);


    /**
     * <p>
     *     This method should prepare a GET request while returning an
     *     {@link BoundRequestBuilder}. The elements to be prepared by
     *     any implementing class are the following ;
     * </p>
     * <ul>
     *     <li>The eventual proxy to use if proxies are enabled</li>
     *     <li>The default headers to use</li>
     *     <li>The default cookies to use</li>
     *     <li>The default POST fields to use</li>
     *     <li>The rotating User Agent header if enabled</li>
     *     <li>The url to scrap the request to</li>
     * </ul>
     * @param url the url to prepare
     * @return the prepared {@link BoundRequestBuilder} ready to be executed.
     */
    BoundRequestBuilder preparePost(String url);

    /**
     * <p>
     *     This method should call {@link #getResponse(BoundRequestBuilder, boolean)}
     *     with {@link HttpManagerConfig#followRedirections} value as the second
     *     parameter.
     * </p>
     * @param req the prepared request to get a response for.
     * @return a string of the response body.
     * @throws RequestTimeoutException when the request timed out on every attempts
     *                                 made. ({@link HttpManagerConfig#getMaxRequestRetries()}
     *                                 will give the number of requests attempted
     *                                 before throwing this exception.
     */
    String getResponse(BoundRequestBuilder req)  throws RequestTimeoutException;


    /**
     * <p>
     *     This method should perform an HTTP request with the prepared
     *     {@link BoundRequestBuilder} request. This should respect all
     *     conditions given in {@link HttpManagerConfig}. This include :
     * </p>
     * <ul>
     *     <li>
     *         Each consecutive request, even when they are performed in
     *         separate threads must be synchronized on the same unique
     *         method so that they wait a minimum time between each request
     *         specified in {@link HttpManagerConfig#getAwaitBetweenRequests()}
     *      </li>
     *      <li>
     *          Respect the given proxy change rate {@link HttpManagerConfig#getProxyChangeRate()}.
     *      </li>
     *      <li>Respect the timeout given by {@link HttpManagerConfig#getTimeout()}</li>
     *      <li>Rotate User Agent header if required by {@link HttpManagerConfig#rotateUserAgent()}</li>
     *      <li>
     *          Retries timed out request at least and not more than :
     *          {@link HttpManagerConfig#getMaxRequestRetries()}
     *      </li>
     *      <li>
     *          Use proxies if required by {@link HttpManagerConfig#useProxy()} with the
     *          provided {@link ProxyFinder}.
     *      </li>
     *      <li>
     *          Connect (TCP Connect) to the url before making the actual HTTP request
     *          if required by {@link HttpManagerConfig#connectToProxyBeforeRequest()}
     *      </li>
     *      <li>
     *          Follow HTTP 301 and 302 redirections if required by {@code followRedirections}
     *          parameter of this method. It should follow redirections maximum once except if
     *          {@link HttpManagerConfig#allowInfiniteRedirections()} is enabled.
     *      </li>
     * </ul>
     * @param req the prepared request to get a response for.
     * @param followRedirections to stipulate if HTTP redirections should be followed.
     * @return a string of the response body.
     * @throws RequestTimeoutException when the request timed out on every attempts
     *                                 made. ({@link HttpManagerConfig#getMaxRequestRetries()}
     *                                 will give the number of requests attempted
     *                                 before throwing this exception.
     */
    String getResponse(BoundRequestBuilder req, boolean followRedirections)  throws RequestTimeoutException;

}
