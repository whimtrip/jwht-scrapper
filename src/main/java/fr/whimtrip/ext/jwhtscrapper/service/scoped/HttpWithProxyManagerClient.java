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

import fr.whimtrip.ext.jwhtscrapper.exception.RequestTimeoutException;
import fr.whimtrip.ext.jwhtscrapper.service.RotatingUserAgent;
import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.base.RequestChecker;
import fr.whimtrip.ext.jwhtscrapper.service.holder.Field;
import fr.whimtrip.ext.jwhtscrapper.service.holder.HttpManagerConfig;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.req.HttpConnectHandler;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.req.RequestCoreHandler;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.req.RequestUtils;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;

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
public class HttpWithProxyManagerClient implements HttpManagerClient{

    private static final String USER_AGENT_HEADER_NAME = "User-Agent";

    private final HttpManagerConfig httpManagerConfig;

    private final RequestChecker requestChecker;

    private final AsyncHttpClient asyncHttpClient;


    /**
     * <p>
     *     Default Package Private constructor that should only be used through its
     *     dedicated builder {@link DefaultHttpManagerClientBuilder}.
     * </p>
     * @param httpManagerConfig the {@link HttpManagerConfig} that will set the rules
     *                          of all HTTP interactions with the urls to scrap.
     * @param requestChecker the {@link RequestChecker} implementation that will help
     *                       synchronyzing the wait between each request to ensure it
     *                       doesn't overlap with concurrent access to this manager.
     * @param asyncHttpClient the Http client that will be used to perform all HTTP
     *                        requests.
     */
    HttpWithProxyManagerClient(
            @NotNull final HttpManagerConfig httpManagerConfig,
            @NotNull final RequestChecker requestChecker,
            @NotNull final AsyncHttpClient asyncHttpClient
    ){
        this.httpManagerConfig = httpManagerConfig;
        this.requestChecker = requestChecker;
        this.asyncHttpClient = asyncHttpClient;
    }


    /**
     * @see HttpManagerClient#prepareGet(String) 
     * @param url the url to prepare
     * @return see {@link HttpManagerClient#prepareGet(String)}
     */
    @Override
    public BoundRequestBuilder prepareGet(String url)
    {
        return prepareRequest(url, asyncHttpClient.prepareGet(url));
    }


    /**
     * @see HttpManagerClient#preparePost(String)
     * @param url the url to prepare
     * @return see {@link HttpManagerClient#preparePost(String)}
     */
    @Override
    public BoundRequestBuilder preparePost(String url)
    {
        BoundRequestBuilder req = prepareRequest(url, asyncHttpClient.preparePost(url));
        if(httpManagerConfig.getFields() != null) {
            for (Field fld :  httpManagerConfig.getFields()) {
                req.addFormParam(fld.getName(), fld.getValue());
            }
        }

        return req;
    }

    /**
     * @see HttpManagerClient#getResponse(BoundRequestBuilder)
     * @param req the prepared request to get a response for.
     * @return the body of the http Response
     * @throws RequestTimeoutException see {@link HttpManagerClient#getResponse(BoundRequestBuilder)}
     */
    @Override
    public String getResponse(BoundRequestBuilder req)  throws RequestTimeoutException {
        return getResponse( req, httpManagerConfig.followRedirections());
    }


    /**
     * @see HttpManagerClient#getResponse(BoundRequestBuilder, boolean)
     * @param req the prepared request to get a response for.
     * @param followRedirections to stipulate if HTTP redirections should be followed.
     * @return the body of the http Response
     * @throws RequestTimeoutException see {@link HttpManagerClient#getResponse(BoundRequestBuilder, boolean)}
     */
    @Override
    public String getResponse(BoundRequestBuilder req, boolean followRedirections)  throws RequestTimeoutException {
        return buildRequestCoreHandler(req, followRedirections).getResponse();
    }


    /**
     * @return the duration in ms since the last request was performed.
     */
    public Long getLastRequest() {
        return requestChecker.getLastRequest();
    }

    /**
     * @return the number of requests made since the last proxy change was made.
     */
    public int getLastProxyChange() {
        return requestChecker.getLastProxyChange();
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

        req.setCookies(httpManagerConfig.getDefaultCookies());
        if(httpManagerConfig.useProxy())
        {
            if(httpManagerConfig.connectToProxyBeforeRequest())
                buildHttpConnectHandler(url, req).tryConnect();
            else
                RequestUtils.resetProxy(req, httpManagerConfig.getProxyFinder());
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

        HttpHeaders headers = httpManagerConfig
                .getBoundRequestBuilderProcessor()
                .newHttpHeaders(httpManagerConfig.getDefaultHeaders());
        return httpManagerConfig.rotateUserAgent() ?
                headers : headers.add(USER_AGENT_HEADER_NAME, RotatingUserAgent.pickRandomUserAgent());
    }


    /**
     * @param req the prepared {@link BoundRequestBuilder} HTTP request.
     * @param followRedirections wether redirections should be followed or not.
     * @return an {@link RequestCoreHandler} instance to perform the request
     *         properly if. Instanciating the request here allows us to define all
     *         of the requests retrying, proxies dump and reset, wait between requests,
     *         exceptions handling, redirections... in a single place containing
     *         information for a given request scope to use OOP efficiency at its best.
     *         previous implementations was done within this class and presented
     *         many problems, mainly with code readability and maintainability.
     */
    @NotNull
    private RequestCoreHandler buildRequestCoreHandler(BoundRequestBuilder req, boolean followRedirections) {
        String url = httpManagerConfig.getBoundRequestBuilderProcessor().getUrlFromRequestBuilder(req);
        return new RequestCoreHandler(
                    httpManagerConfig,
                    requestChecker,
                    buildHttpConnectHandler(url, req),
                    req,
                    url,
                    followRedirections
        );
    }


    /**
     * @param url the url to prepare the connection to
     * @param req the prepared {@link BoundRequestBuilder} HTTP request.
     * @return built in request scoped {@link HttpConnectHandler} for TCP Connect
     *         previous step when required by {@link HttpManagerConfig#connectToProxyBeforeRequest()}.
     */
    @NotNull
    private HttpConnectHandler buildHttpConnectHandler(String url, BoundRequestBuilder req) {

        return new HttpConnectHandler(httpManagerConfig, asyncHttpClient, req, url);
    }


}
