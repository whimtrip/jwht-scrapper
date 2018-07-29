/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.annotation.ProxyConfig;
import fr.whimtrip.ext.jwhtscrapper.annotation.RequestsConfig;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.base.RequestSynchronizer;
import fr.whimtrip.ext.jwhtscrapper.service.holder.PostField;
import fr.whimtrip.ext.jwhtscrapper.service.holder.HttpManagerConfig;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.req.RequestSynchronizerImpl;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import org.asynchttpclient.AsyncHttpClient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     The {@link HttpManagerClient} default builder providing with
 *     default implementation {@link HttpWithProxyManagerClient}.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class DefaultHttpManagerClientBuilder {



    private int awaitBetweenRequests = RequestsConfig.DEFAULT_WAIT_BETWEEN_REQUESTS;
    private int proxyChangeRate = ProxyConfig.PROXY_CHANGE_RATE;
    private int timeout = RequestsConfig.DEFAULT_TIMEOUT;
    private boolean useProxy = true;
    private boolean connectToProxyBeforeRequest = false;
    private boolean allowInfiniteRedirections = false;
    private boolean rotatingUserAgent = false;
    private boolean followRedirections;
    private int maxRequestRetries = RequestsConfig.DEFAULT_MAX_REQUEST_RETRIES;
    private final ExceptionLogger exceptionLogger;
    private final AsyncHttpClient asyncHttpClient;
    private HttpHeaders defaultHeaders;
    private ProxyFinder proxyFinder;
    private Cookie[] defaultCookies = new Cookie[]{};
    private BoundRequestBuilderProcessor boundRequestBuilderProcessor;
    private List<PostField> defaultFields;

    /**
     *
     * @param asyncHttpClient the http client to use by the {@link HttpManagerClient}
     * @param exceptionLogger the exception logger to use by this {@link HttpManagerClient}
     * @param boundRequestBuilderProcessor the request processor.
     */
    public DefaultHttpManagerClientBuilder(
            @NotNull  final AsyncHttpClient asyncHttpClient,
            @NotNull  final ExceptionLogger exceptionLogger,
            @NotNull  final BoundRequestBuilderProcessor boundRequestBuilderProcessor
    ){
        this.asyncHttpClient = asyncHttpClient;
        this.exceptionLogger = exceptionLogger;
        this.boundRequestBuilderProcessor = boundRequestBuilderProcessor;
    }

    /**
     * @param awaitBetweenRequests the minimum time to wait between each request.
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder setAwaitBetweenRequests(int awaitBetweenRequests) {
        this.awaitBetweenRequests = awaitBetweenRequests;
        return this;
    }

    /**
     * @param proxyChangeRate the rate at which the proxy should be changed.
     *                        {@code 8} for example would mean that it will be changed
     *                        every {@code 8} requests.
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder setProxyChangeRate(int proxyChangeRate) {
        this.proxyChangeRate = proxyChangeRate;
        return this;
    }

    /**
     * @param timeout the timeout after which each single request will be considered
     *                as failed.
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * @param useProxy wether proxies should be used or not. If set to true a {@link ProxyFinder}
     *                 should be provided.
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
        return this;
    }

    /**
     * @param connectToProxyBeforeRequest wether a TCP CONNECT operation should be performed
     *                                    before the HTTP request or not.
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder setConnectToProxyBeforeRequest(boolean connectToProxyBeforeRequest) {
        this.connectToProxyBeforeRequest = connectToProxyBeforeRequest;
        return this;
    }

    /**
     * @param maxRequestRetries the maximum number of HTTP request retry before an exception
     *                          will be propagated to the upper layer.
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder setMaxRequestRetries(int maxRequestRetries) {
        this.maxRequestRetries = maxRequestRetries;
        return this;
    }

    /**
     * @param defaultHeaders the default HTTP headers to use on each request. If {@code rotatingUserAgent}
     *                       was set to true, no {@code User-Agent} header should be provided in those
     *                       HttpHeaders.
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder setDefaultHeaders(HttpHeaders defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
        return this;
    }

    /**
     * @see #setDefaultHeaders(HttpHeaders)
     * @param name the name of the header to add
     * @param value the value of this header
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder addDefaultHeader(String name, String value) {
        if(defaultHeaders == null)
            this.defaultHeaders = new DefaultHttpHeaders();
        this.defaultHeaders.add(name, value);
        return this;
    }

    /**
     * @param defaultCookies the default cookies to use on each request.
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder setDefaultCookies(Cookie... defaultCookies) {
        this.defaultCookies = defaultCookies;
        return this;
    }

    /**
     * @see ProxyFinder
     * @param proxyFinder the proxy finder to be used by the {@link HttpManagerClient}.
     *                    If {@code setUseProxy} was set to true, a proxy finder must
     *                    be submitted.
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder setProxyFinder(ProxyFinder proxyFinder) {
        this.proxyFinder = proxyFinder;
        return this;
    }

    /**
     * @param allowInfiniteRedirections wether infinite HTTP redirection (301 and 302 HTTP status)
     *                                  should be accepted or not.
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder setAllowInfiniteRedirections(boolean allowInfiniteRedirections) {

        this.allowInfiniteRedirections = allowInfiniteRedirections;
        return this;
    }

    /**
     * @param followRedirections wether HTTP redirection (301 and 302 HTTP status)
     *                           should be accepted or not. If false, no redirection
     *                           will be followed, even though {@code allowInfiniteRedirections}
     *                           is set to true. If set to true with {@code allowInfiniteRedirections}
     *                           set to false, redirections will only be followed once
     *                           in per single HTTP request but not more.
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder setFollowRedirections(boolean followRedirections) {

        this.followRedirections = followRedirections;
        return this;
    }

    /**
     * @param rotatingUserAgent Wether User-Agent should automatically rotate between each request or not.
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder setRotatingUserAgent(boolean rotatingUserAgent) {

        this.rotatingUserAgent = rotatingUserAgent;
        return this;
    }



    /**
     * @param defaultFields Default fields to use for the builder.
     * @return the current builder instance.
     */
    public DefaultHttpManagerClientBuilder setDefaultFields(List<PostField> defaultFields) {

        this.defaultFields = defaultFields;
        return this;
    }

    /**
     * @return the built {@link HttpManagerClient} from default implementation {@link HttpWithProxyManagerClient}
     *         with the current builder.
     */
    public HttpManagerClient build() {
        HttpManagerConfig httpManagerConfig = new HttpManagerConfig(
                exceptionLogger,
                proxyFinder, boundRequestBuilderProcessor,
                awaitBetweenRequests,
                proxyChangeRate,
                timeout,
                useProxy,
                connectToProxyBeforeRequest,
                rotatingUserAgent,
                allowInfiniteRedirections,
                followRedirections,
                maxRequestRetries,
                defaultHeaders,
                defaultFields,
                defaultCookies
        );

        RequestSynchronizer requestSynchronizer = new RequestSynchronizerImpl(httpManagerConfig);

        return new HttpWithProxyManagerClient(httpManagerConfig, requestSynchronizer, asyncHttpClient);
    }

}