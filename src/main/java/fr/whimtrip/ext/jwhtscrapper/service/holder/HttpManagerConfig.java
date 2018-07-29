package fr.whimtrip.ext.jwhtscrapper.service.holder;

import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import fr.whimtrip.ext.jwhtscrapper.service.RotatingUserAgent;
import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.BoundRequestBuilderProcessor;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.HttpWithProxyManagerClient;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * <p>
 *     The config holder class that will hold all http requests related
 *     configurations so that they can be easily accessed and used by
 *     {@link HttpWithProxyManagerClient} and its subsequent classes.
 * </p>
 *
 * @see HttpWithProxyManagerClient
 * @author Louis-wht
 * @since 1.0.0
 */
public class HttpManagerConfig {



    private HttpHeaders defaultHeaders;
    private final List<Cookie> defaultCookies;

    private final int awaitBetweenRequests;
    private final int proxyChangeRate;
    private final int timeout;
    private final boolean rotatingUserAgent;
    private final int maxRequestRetries;
    private final boolean useProxy;
    private final List<Field> defaultFields;
    private final boolean connectToProxyBeforeRequest;
    private final boolean allowInfiniteRedirections;
    private final boolean followRedirections;


    private final ExceptionLogger exceptionLogger;

    private final ProxyFinder proxyFinder;

    private final BoundRequestBuilderProcessor boundRequestBuilderProcessor;


    /**
     *
     * @param exceptionLogger the exception logger to use by the correct {@link HttpManagerClient}
     * @param proxyFinder the proxy finder to use if {@code useProxy} is enabled.
     *                    see {@link ProxyFinder}.
     * @param boundRequestBuilderProcessor the request processor instance to use.
     * @param awaitBetweenRequests time to wait between each consecutive http
     *                             request. <p></p>
     *
     * @param proxyChangeRate the rate at which the proxies should be switched
     *
     * @param timeout the timeout in milliseconds before the request will be
     *                retried <p></p>
     *
     * @param useProxy wether you should use proxies or not for performing your
     *                request<p></p>
     *
     * @param connectToProxyBeforeRequest wether a {@code CONNECT} TCP initialization
     * request should be performed before hand.
     * <strong>Warning! Only use if you know what you are doing!</strong><p></p>
     *
     * @param rotatingUserAgent will auto assign rotating user agent headers to
     *  each request using {@link RotatingUserAgent#pickRandomUserAgent()}.<p></p>
     *
     * @param allowInfiniteRedirections will allow infinite redirections.
     *  Redirections with {@code 301} or {@code 302} HTTP Status codes will
     *  be followed as a normal browser would. Redirections are by default
     *  limited to 3 on the same request. Setting this field to true will
     *  let potential (quite common case when scrapping) happens.
     *  <strong>Warning! Only use if you know what you are doing!</strong><p></p>
     *
     * @param followRedirections wether HTTP redirection (301 and 302 HTTP status)
     * should be accepted or not. If false, no redirection will be followed, even
     * though {@code allowInfiniteRedirections} is set to true. If set to true with
     * {@code allowInfiniteRedirections} set to false, redirections will only be
     * followed once in per single HTTP request but not more.<p></p>
     *
     * @param maxRequestRetries maximum number of retries before throwing a
     *                          failure exception<p></p>
     *
     *
     * @param defaultHeaders default headers to use in each requests<p></p>
     * @param defaultCookies default cookies to use in each requests<p></p>
     * @param defaultFields default POST defaultFields to use on each requests.
     */
    public HttpManagerConfig(
            ExceptionLogger exceptionLogger,
            ProxyFinder proxyFinder,
            BoundRequestBuilderProcessor boundRequestBuilderProcessor,
            int awaitBetweenRequests,
            int proxyChangeRate,
            int timeout,
            boolean useProxy,
            boolean connectToProxyBeforeRequest,
            boolean rotatingUserAgent,
            boolean allowInfiniteRedirections,
            boolean followRedirections,
            int maxRequestRetries,
            HttpHeaders defaultHeaders,
            List<Field> defaultFields,
            Cookie... defaultCookies
    ){

        this.exceptionLogger = exceptionLogger;
        this.proxyFinder = proxyFinder;
        this.boundRequestBuilderProcessor = boundRequestBuilderProcessor;
        this.awaitBetweenRequests = awaitBetweenRequests;
        this.proxyChangeRate = proxyChangeRate;
        this.timeout = timeout;
        this.rotatingUserAgent = rotatingUserAgent;
        this.allowInfiniteRedirections = allowInfiniteRedirections;
        this.followRedirections = followRedirections;
        this.maxRequestRetries = maxRequestRetries;
        this.connectToProxyBeforeRequest = connectToProxyBeforeRequest;

        this.useProxy = useProxy;
        this.defaultFields = defaultFields;

        this.defaultCookies = new ArrayList<>();
        this.defaultCookies.addAll(Arrays.asList(defaultCookies));

        this.defaultHeaders = new DefaultHttpHeaders();

        if(defaultHeaders != null)
            this.defaultHeaders.add(defaultHeaders);

    }


    /**
     * @return default headers to use in each requests
     */
    public HttpHeaders getDefaultHeaders() {

        return defaultHeaders;
    }

    /**
     * @return default cookies to use in each requests
     */
    public List<Cookie> getDefaultCookies() {

        return defaultCookies;
    }


    /**
     * @return default POST defaultFields to use on each requests.
     */
    public List<Field> getDefaultFields() {

        return defaultFields;
    }

    /**
     * @return time to wait between each consecutive http request.
     */
    public int getAwaitBetweenRequests() {

        return awaitBetweenRequests;
    }

    /**
     * @return  the rate at which the proxies should be switched.
     */
    public int getProxyChangeRate() {

        return proxyChangeRate;
    }

    /**
     * @return the timeout in milliseconds before the request will be retried.
     */
    public int getTimeout() {

        return timeout;
    }

    /**
     * @return will auto assign rotating user agent headers to
     *  each request using {@link RotatingUserAgent#pickRandomUserAgent()}.
     */
    public boolean rotateUserAgent() {

        return rotatingUserAgent;
    }

    /**
     * @return maximum number of retries before throwing a failure exception.
     */
    public int getMaxRequestRetries() {

        return maxRequestRetries;
    }

    /**
     * @return  wether you should use proxies or not for performing your requests.
     */
    public boolean useProxy() {

        return useProxy;
    }

    /**
     * @return wether a {@code CONNECT} TCP initialization request should be
     * performed before hand.
     * <strong>Warning! Only use if you know what you are doing!</strong><p></p>
     */
    public boolean connectToProxyBeforeRequest() {

        return connectToProxyBeforeRequest;
    }

    /**
     * @return  wether HTTP redirection (301 and 302 HTTP status)
     * should be accepted or not. If false, no redirection will be followed, even
     * though {@code allowInfiniteRedirections} is set to true. If set to true with
     * {@code allowInfiniteRedirections} set to false, redirections will only be
     * followed once in per single HTTP request but not more.
     */
    public boolean followRedirections() {
        return followRedirections;
    }

    /**
     * @return will allow infinite redirections.
     *  Redirections with {@code 301} or {@code 302} HTTP Status codes will
     *  be followed as a normal browser would. Redirections are by default
     *  limited to 3 on the same request. Setting this field to true will
     *  let potential (quite common case when scrapping) happens.
     *  <strong>Warning! Only use if you know what you are doing!</strong><p></p>
     */
    public boolean allowInfiniteRedirections() {

        return allowInfiniteRedirections;
    }

    /**
     * @return the exception logger to use by the correct {@link HttpManagerClient}.
     */
    public ExceptionLogger getExceptionLogger() {

        return exceptionLogger;
    }

    /**
     * @return the proxy finder to use if {@code useProxy} is enabled.
     *         See {@link ProxyFinder}.
     */
    public ProxyFinder getProxyFinder() {

        return proxyFinder;
    }

    /**
     * @return  the request processor instance to use.
     */
    public BoundRequestBuilderProcessor getBoundRequestBuilderProcessor() {

        return boundRequestBuilderProcessor;
    }

}
