/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.ext.jwhtscrapper.exception.RequestMaxRetriesReachedException;
import fr.whimtrip.ext.jwhtscrapper.intfr.HttpMetrics;
import fr.whimtrip.ext.jwhtscrapper.intfr.Proxy;
import fr.whimtrip.ext.jwhtscrapper.service.RotatingUserAgent;
import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.base.RequestSynchronizer;
import fr.whimtrip.ext.jwhtscrapper.service.holder.HttpManagerConfig;
import fr.whimtrip.ext.jwhtscrapper.service.holder.PostField;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.req.HttpConnectHandler;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.req.RequestCoreHandler;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.req.RequestUtils;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     The {@link HttpManagerClient} default implementation.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public final class HttpWithProxyManagerClient implements HttpManagerClient{


    private static final String USER_AGENT_HEADER_NAME = "User-Agent";

    private final HttpManagerConfig httpManagerConfig;

    private final RequestSynchronizer requestSynchronizer;

    private final AsyncHttpClient asyncHttpClient;

    private final Map<BoundRequestBuilder, RequestCoreHandler> contextHolder = new HashMap<>();



    /**
     * <p>
     *     Default Package Private constructor that should only be used through its
     *     dedicated builder {@link DefaultHttpManagerClientBuilder}.
     * </p>
     * @param httpManagerConfig the {@link HttpManagerConfig} that will set the rules
     *                          of all HTTP interactions with the urls to scrap.
     * @param requestSynchronizer the {@link RequestSynchronizer} implementation that will help
     *                       synchronyzing the wait between each request to ensure it
     *                       doesn't overlap with concurrent access to this manager.
     * @param asyncHttpClient the Http client that will be used to perform all HTTP
     */
    HttpWithProxyManagerClient(@NotNull final HttpManagerConfig httpManagerConfig, @NotNull final RequestSynchronizer requestSynchronizer, @NotNull final AsyncHttpClient asyncHttpClient){
        this.httpManagerConfig = httpManagerConfig;
        this.requestSynchronizer = requestSynchronizer;
        this.asyncHttpClient = asyncHttpClient;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public BoundRequestBuilder prepareGet(String url)
    {
        return prepareRequest(url, asyncHttpClient.prepareGet(url));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public BoundRequestBuilder preparePost(String url)
    {
        BoundRequestBuilder req = prepareRequest(url, asyncHttpClient.preparePost(url));
        if(httpManagerConfig.getDefaultFields() != null) {
            for (PostField fld :  httpManagerConfig.getDefaultFields()) {
                req.addFormParam(fld.getName(), fld.getValue());
            }
        }

        return req;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResponse(BoundRequestBuilder req)  throws RequestMaxRetriesReachedException {
        return getResponse( req, httpManagerConfig.followRedirections());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getResponse(BoundRequestBuilder req, boolean followRedirections)  throws RequestMaxRetriesReachedException {

        RequestCoreHandler requestCoreHandler = null;

        synchronized (contextHolder) {
            requestCoreHandler = contextHolder.get(req);
        }

        String response =
                requestCoreHandler
                    .setFollowRedirections(followRedirections)
                    .getResponse();


        return response;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public HttpMetrics getHttpMetrics(){
        return requestSynchronizer.getHttpMetrics();
    }

    /**
     * {@inheritDoc}
     */
    public Long getLastRequest() {
        return requestSynchronizer.getLastRequest();
    }

    /**
     * {@inheritDoc}
     */
    public int getLastProxyChange() {
        return requestSynchronizer.getLastProxyChange();
    }

    /**
     * {@inheritDoc}
     */
    public void removeContext(@NotNull final BoundRequestBuilder req) {
        synchronized (contextHolder) {
            contextHolder.remove(req);
        }
    }

    /**
     * <p>Inner method for request preparation shared between POST requests and GET requests</p>
     * @param url the url to make the request to.
     * @param req the pre request builder (which only has the HTTP method at this point).
     * @return the prepared {@link BoundRequestBuilder} (or almost prepared for requests
     *         with body which needs some additional tuning).
     */
    private BoundRequestBuilder prepareRequest(String url, BoundRequestBuilder req)
    {
        req.resetCookies();
        req.clearHeaders();
        req.setHeaders(buildDefaultHeaders());

        Proxy proxy = null;

        req.setCookies(httpManagerConfig.getDefaultCookies());
        HttpConnectHandler httpConnectHandler = null;
        if(httpManagerConfig.useProxy())
        {
            proxy = RequestUtils.resetProxy(req, httpManagerConfig.getProxyFinder());
            if(httpManagerConfig.connectToProxyBeforeRequest()) {
                httpConnectHandler = buildHttpConnectHandler(url, req, proxy);
                httpConnectHandler.tryConnect();
            }

        }

        synchronized (contextHolder) {
            contextHolder.put(req, buildRequestCoreHandler(url, proxy, req, httpConnectHandler));
        }
        return req;
    }


    /**
     * @return Built http headers. As {@link HttpManagerConfig#getDefaultHeaders()}
     *         is the same instance accross our whole application, and as it is not
     *         a mutable object, we have to copy it into a new http headers object
     *         in order to be able to customize it, particularly for user defined
     *         hooks that will add custom headers to ensure they don't add those
     *         headers on all requests and therefore break the scrapping.
     */
    private HttpHeaders buildDefaultHeaders() {

        HttpHeaders headers = new DefaultHttpHeaders()
                                    .add(httpManagerConfig.getDefaultHeaders());

        return httpManagerConfig.rotateUserAgent() ?
                headers : headers.add(USER_AGENT_HEADER_NAME, RotatingUserAgent.pickRandomUserAgent());
    }


    /**
     * @param url the url to scrap
     * @param proxy the proxy used to connect beforehand
     * @param req the prepared {@link BoundRequestBuilder} HTTP request.
     * @param httpConnectHandler the http connect handler if any.
     * @return an {@link RequestCoreHandler} instance to perform the request
     *         properly if. Instanciating the request here allows us to define all
     *         of the requests retrying, proxies dump and reset, wait between requests,
     *         exceptions handling, redirections... in a single place containing
     *         information for a given request scope to use OOP efficiency at its best.
     *         previous implementations was done within this class and presented
     *         many problems, mainly with code readability and maintainability.
     */
    @NotNull
    private RequestCoreHandler buildRequestCoreHandler(String url, Proxy proxy, BoundRequestBuilder req, HttpConnectHandler httpConnectHandler) {
        return new RequestCoreHandler(
                    httpManagerConfig,
                    requestSynchronizer,
                    httpConnectHandler,
                    req,
                    proxy,
                    url
        );
    }


    /**
     * @param url the url to prepare the connection to
     * @param req the prepared {@link BoundRequestBuilder} HTTP request.
     * @param proxy the proxy used to connect beforehand
     * @return built in request scoped {@link HttpConnectHandler} for TCP Connect
     *         previous step when required by {@link HttpManagerConfig#connectToProxyBeforeRequest()}.
     */
    @NotNull
    private HttpConnectHandler buildHttpConnectHandler(String url, BoundRequestBuilder req, Proxy proxy) {

        return new HttpConnectHandler(httpManagerConfig, asyncHttpClient, requestSynchronizer, req, url, proxy);
    }


}
