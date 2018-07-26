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
import fr.whimtrip.ext.jwhtscrapper.intfr.BasicObjectMapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import org.asynchttpclient.AsyncHttpClient;
import org.jetbrains.annotations.NotNull;

public class ProxyManagerClientBuilder{



    private int awaitBetweenRequests = RequestsConfig.DEFAULT_WAIT_BETWEEN_REQUESTS;
    private int proxyChangeRate = ProxyConfig.PROXY_CHANGE_RATE;
    private int timeout = RequestsConfig.DEFAULT_TIMEOUT;
    private boolean useProxy = true;
    private boolean connectToProxyBeforeRequest = false;
    private boolean allowInfiniteRedirections = false;
    private boolean rotatingUserAgent = false;
    private int maxRequestRetries = RequestsConfig.DEFAULT_MAX_REQUEST_RETRIES;
    private final BasicObjectMapper objectMapper;
    private final ExceptionLogger exceptionLogger;
    private final AsyncHttpClient asyncHttpClient;
    private HttpHeaders defaultHeaders;
    private ProxyFinder proxyFinder;
    private Cookie[] defaultCookies = new Cookie[]{};
    private BoundRequestBuilderProcessor boundRequestBuilder;

    public ProxyManagerClientBuilder(
            @NotNull final AsyncHttpClient asyncHttpClient,
            @NotNull final BasicObjectMapper objectMapper,
            @NotNull final ExceptionLogger exceptionLogger,
            @NotNull final BoundRequestBuilderProcessor boundRequestBuilder
    ){
        this.asyncHttpClient = asyncHttpClient;
        this.objectMapper = objectMapper;
        this.exceptionLogger = exceptionLogger;
        this.boundRequestBuilder = boundRequestBuilder;
    }

    public ProxyManagerClientBuilder setAwaitBetweenRequests(int awaitBetweenRequests) {
        this.awaitBetweenRequests = awaitBetweenRequests;
        return this;
    }

    public ProxyManagerClientBuilder setProxyChangeRate(int proxyChangeRate) {
        this.proxyChangeRate = proxyChangeRate;
        return this;
    }

    public ProxyManagerClientBuilder setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public ProxyManagerClientBuilder setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
        return this;
    }

    public ProxyManagerClientBuilder setConnectToProxyBeforeRequest(boolean connectToProxyBeforeRequest) {
        this.connectToProxyBeforeRequest = connectToProxyBeforeRequest;
        return this;
    }

    public ProxyManagerClientBuilder setMaxRequestRetries(int maxRequestRetries) {
        this.maxRequestRetries = maxRequestRetries;
        return this;
    }

    public ProxyManagerClientBuilder setDefaultHeaders(HttpHeaders defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
        return this;
    }

    public ProxyManagerClientBuilder addDefaultHeader(String name, String value) {
        if(defaultHeaders == null)
            this.defaultHeaders = new DefaultHttpHeaders();
        this.defaultHeaders.add(name, value);
        return this;
    }

    public ProxyManagerClientBuilder setDefaultCookies(Cookie... defaultCookies) {
        this.defaultCookies = defaultCookies;
        return this;
    }

    public ProxyManagerClientBuilder setProxyFinder(ProxyFinder proxyFinder) {
        this.proxyFinder = proxyFinder;
        return this;
    }

    public ProxyManagerClientBuilder setAllowInfiniteRedirections(boolean allowInfiniteRedirections) {

        this.allowInfiniteRedirections = allowInfiniteRedirections;
        return this;
    }

    public ProxyManagerClientBuilder setRotatingUserAgent(boolean rotatingUserAgent) {

        this.rotatingUserAgent = rotatingUserAgent;
        return this;
    }

    public ProxyManagerClient build() {
        return new ProxyManagerClient(
                objectMapper,
                exceptionLogger,
                asyncHttpClient,
                proxyFinder,
                boundRequestBuilder,
                awaitBetweenRequests,
                proxyChangeRate,
                timeout,
                useProxy,
                connectToProxyBeforeRequest,
                rotatingUserAgent,
                allowInfiniteRedirections,
                maxRequestRetries,
                defaultHeaders,
                defaultCookies
        );
    }

}