package fr.whimtrip.ext.jwhtscrapper.service.holder;

import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.BoundRequestBuilderProcessor;
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
    private final List<Field> fields;
    private final boolean connectToProxyBeforeRequest;
    private final boolean allowInfiniteRedirections;
    private final boolean followRedirections;


    private final ExceptionLogger exceptionLogger;

    private final ProxyFinder proxyFinder;

    private final BoundRequestBuilderProcessor boundRequestBuilderProcessor;


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
            List<Field> fields,
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
        this.fields = fields;

        this.defaultCookies = new ArrayList<>();
        this.defaultCookies.addAll(Arrays.asList(defaultCookies));

        this.defaultHeaders = new DefaultHttpHeaders();

        if(defaultHeaders != null)
            this.defaultHeaders.add(defaultHeaders);

    }


    public HttpHeaders getDefaultHeaders() {

        return defaultHeaders;
    }

    public List<Cookie> getDefaultCookies() {

        return defaultCookies;
    }


    public List<Field> getFields() {

        return fields;
    }

    public int getAwaitBetweenRequests() {

        return awaitBetweenRequests;
    }

    public int getProxyChangeRate() {

        return proxyChangeRate;
    }

    public int getTimeout() {

        return timeout;
    }

    public boolean rotateUserAgent() {

        return rotatingUserAgent;
    }

    public int getMaxRequestRetries() {

        return maxRequestRetries;
    }

    public boolean useProxy() {

        return useProxy;
    }

    public boolean connectToProxyBeforeRequest() {

        return connectToProxyBeforeRequest;
    }

    public boolean followRedirections() {
        return followRedirections;
    }

    public boolean allowInfiniteRedirections() {

        return allowInfiniteRedirections;
    }

    public ExceptionLogger getExceptionLogger() {

        return exceptionLogger;
    }

    public ProxyFinder getProxyFinder() {

        return proxyFinder;
    }

    public BoundRequestBuilderProcessor getBoundRequestBuilderProcessor() {

        return boundRequestBuilderProcessor;
    }

}
