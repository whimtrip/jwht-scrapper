package fr.whimtrip.ext.jwhtscrapper.service.scoped.req;

import fr.whimtrip.ext.jwhtscrapper.exception.RequestFailedException;
import fr.whimtrip.ext.jwhtscrapper.exception.RequestMaxRetriesReachedException;
import fr.whimtrip.ext.jwhtscrapper.exception.RequestTimeoutException;
import fr.whimtrip.ext.jwhtscrapper.intfr.Proxy;
import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.base.RequestSynchronizer;
import fr.whimtrip.ext.jwhtscrapper.service.holder.HttpManagerConfig;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static fr.whimtrip.ext.jwhtscrapper.intfr.Proxy.Status.*;
import static fr.whimtrip.ext.jwhtscrapper.service.holder.StatusRange.TIMEOUT_STATUS_CODE;
import static fr.whimtrip.ext.jwhtscrapper.service.holder.StatusRange.UNKNOWN_EXCEPTION_STATUS_CODE;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * <p>
 *     Core processing unit that will perform all underlying HTTP requests.
 *     This processing unit will be instanciated once per HTTP request
 *     (being a lightweight instance, this won't cause any problem), and will
 *     handle all proper considerations required by {@link HttpManagerConfig}.
 *     The contract to respect can be found in the documentation of
 *     {@link HttpManagerClient#getResponse(BoundRequestBuilder, boolean)}.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class RequestCoreHandler {


    private static final Logger log = LoggerFactory.getLogger(RequestCoreHandler.class);

    private final HttpManagerConfig httpManagerConfig;

    private final RequestSynchronizer requestSynchronizer;

    private final HttpConnectHandler httpConnectHandler;

    private final BoundRequestBuilder req;


    private boolean followRedirections;

    private Proxy actualProxy;

    private boolean firstRedirection = true;

    private boolean firstTry = true;

    private int tries = 0;

    @Nullable
    private Proxy proxy;
    private String url;

    private int lastStatusCode = 0;

    /**
     * <p>
     *     This constructor includes request scoped and {@link HttpManagerClient} scoped
     *     services required to perform correctly the request.
     *     This include :
     * </p>
     * @param httpManagerConfig the requests configurations.<br>
     * @param requestSynchronizer the synchronyzed checker that will help every concurrent
     *                       access to this class to be synchronyzed on the same
     *                       object instance to ensure no two requests are performed
     *                       without the required {@link HttpManagerConfig#getAwaitBetweenRequests()}
     *                       wait between those two requests.<br>
     * @param httpConnectHandler the connect handler that will handle the connect process is
     *                           done correctly if required by {@link HttpManagerConfig#connectToProxyBeforeRequest()}.<br>
     * @param req the prepared request builder that will be used to execute the HTTP request.<br>
     * @param url the url to scrap.<br>
     * @param proxy the proxy that will be used to forward the HTTP requests.
     */
    public RequestCoreHandler(
            @NotNull  final HttpManagerConfig httpManagerConfig,
            @NotNull  final RequestSynchronizer requestSynchronizer,
            @Nullable final HttpConnectHandler httpConnectHandler,
            @NotNull  final BoundRequestBuilder req,
            @Nullable final Proxy proxy,
            @NotNull  final String url
    ){

        this.httpManagerConfig = httpManagerConfig;
        this.requestSynchronizer = requestSynchronizer;
        this.httpConnectHandler = httpConnectHandler;
        this.req = req;
        this.proxy = proxy;
        this.url = url;
        this.actualProxy = httpManagerConfig.getBoundRequestBuilderProcessor().getProxyServerFromRequestBuilder(req);
        this.followRedirections = httpManagerConfig.followRedirections();
    }

    /**
     *
     * @param followRedirections wether the redirections 301/302 HTTP should be followed
     *                           or not. By default, we should use {@link HttpManagerConfig#followRedirections()}
     *                           but when following sublinks from a given POJO, redirection
     *                           policy can be modified, explaining why this parameter must
     *                           be supplied.
     * @return the actual RequestCoreHandler.
     */
    public RequestCoreHandler setFollowRedirections(boolean followRedirections) {
        this.followRedirections = followRedirections;
        return this;
    }

    /**
     * <p>
     *     The core and only public method of this class that will retrieve the String
     *     body of an HTTP request. It should respects all the contracts specified
     *     here {@link HttpManagerClient#getResponse(BoundRequestBuilder, boolean)}.
     * </p>
     * @return the Stringified body of the HTTP response.
     * @throws RequestMaxRetriesReachedException when the maximum retries count
     *                                           was reached without successful
     *                                           HTTP response.
     */
    public String getResponse() throws RequestMaxRetriesReachedException {

        requestSynchronizer.checkAwaitBetweenRequest(url);

        boolean requestIssued = false;
        String response = "";
        Throwable actE = null;

        if(log.isDebugEnabled()) logRequest();

        try {

            // performing the request
            Response resp = req.execute().get(httpManagerConfig.getTimeout(), TimeUnit.MILLISECONDS);

            lastStatusCode = resp.getStatusCode();

            requestSynchronizer.logHttpStatus(lastStatusCode, firstTry);
            firstTry = false;

            log.debug("Request status : {} {}", lastStatusCode, resp.getStatusText());

            // if this is a redirection.
            if((lastStatusCode == 301 || lastStatusCode == 302) && resp.getHeader("Location") != null)
                return handleRedirection(resp);

            response = resp.getResponseBody();
            requestSynchronizer.incrementLastProxyChange();

            // if the status code is not 2xx successfull
            if(lastStatusCode / 100 != 2) handleFailedRequest(resp);

            requestIssued = true;
        }
        catch (RequestTimeoutException e) {
            actE = e;
            handleTimeoutException();
        }
        catch(RequestFailedException e)
        {
            actE = e;
            handleRequestException();
        }
        catch(Exception e)
        {
            actE = e;
            firstTry = false;
            handleUnknownException();
        }

        if(httpManagerConfig.useProxy() && requestIssued) unfreezeProxy();
        if(!requestIssued) return retryRequest(actE);

        return response;
    }

    /**
     * <p>
     *     Will unfreeze a proxy which was marked as {@link Proxy.Status#FROZEN}
     *     when the requests issued with this proxy was successful.
     * </p>
     */
    private void unfreezeProxy() {

        if (actualProxy != null && actualProxy.getStatus() == FROZEN)
        {
            RequestUtils.setProxyStatus(actualProxy, WORKING, httpManagerConfig.getProxyFinder());
        }
    }

    /**
     * <p>
     *     Method in charge of retrying a request that has been failed. If the
     *     {@link HttpManagerConfig#getMaxRequestRetries()} has been reached, an
     *     exception will be thrown instead.
     * </p>
     * @param actE the latest failure exception thrown
     * @return the String body of the HTTP response
     * @throws RequestMaxRetriesReachedException when the maximum number of retries has
     *                                           been reached.
     */
    private String retryRequest(Throwable actE) throws RequestMaxRetriesReachedException {

        if(tries >= httpManagerConfig.getMaxRequestRetries())
            throw  new RequestMaxRetriesReachedException(
                            actE,
                            url,
                            httpManagerConfig.getMaxRequestRetries(),
                            lastStatusCode
                    );

        return retryRequest();
    }


    /**
     * This method will handle Timeout exceptions in order to properly
     * log them. Under the hood, it will later call {@link #handleRequestException()}.
     */
    private void handleTimeoutException() {
        requestSynchronizer.logHttpStatus(TIMEOUT_STATUS_CODE, firstTry);
        firstTry = false;
        handleRequestException();
    }



    /**
     * This method will handle request exceptions {@link RequestTimeoutException}
     * and {@link RequestFailedException}.
     */
    private void handleRequestException() {
        if(httpManagerConfig.useProxy() && actualProxy != null) {
            Proxy.Status newStatus = actualProxy.getStatus() == WORKING ? FROZEN : BANNED;

            RequestUtils.setProxyStatus(actualProxy, newStatus, httpManagerConfig.getProxyFinder());
            log.info("Proxy {}:{} was {}", actualProxy.getId(), actualProxy.getPort(), newStatus);
        }
    }

    /**
     * This method will handle all other exceptions that might have occured in the
     * request execution scope. IO Exceptions for example might be handled here.
     */
    private void handleUnknownException() {

        requestSynchronizer.incrementLastProxyChange();
        requestSynchronizer.logHttpStatus(UNKNOWN_EXCEPTION_STATUS_CODE, firstTry);
        firstTry = false;
        handleRequestException();
    }

    /**
     * This method will log the request with a debug logger.
     */
    private void logRequest() {

        if(actualProxy == null) {
            log.debug(
                        "Polling : with method {} at url {}.",
                        httpManagerConfig.getBoundRequestBuilderProcessor().getMethod(req),
                        url
            );
        }

        else{
            log.debug(
                    "Polling : {}:{} in {} with method {} at url {}",
                    actualProxy.getIpAdress(),
                    actualProxy.getPort(),
                    actualProxy.getCountryName(),
                    httpManagerConfig.getBoundRequestBuilderProcessor().getMethod(req),
                    url
            );
        }
    }

    /**
     * <p>
     *     This method will perform redirection preparation and request retrying
     *     if redirection is enabled / still enabled (in the case of a n-th redirection.)
     * </p>
     * @param resp the response to perform redirection following instructions for.
     * @return stringified response body for the corresponding redirection if performed
     *         or of the current HTTP Response otherwise.
     */
    private String handleRedirection(Response resp) {

        String newUrl = resp.getHeader("Location");
        log.info("Trying to follow redirections to {} with followRedirections to {}.", newUrl, followRedirections);
        if(followRedirections && (firstRedirection || httpManagerConfig.allowInfiniteRedirections())) {
            req.setUrl(newUrl);
            url = newUrl;
            firstRedirection = false;
            return retryRequest();
        }
        else
        {
            log.info("Redirection required that cannot be followed with body :  {}.", resp.getResponseBody());
            httpManagerConfig.getBoundRequestBuilderProcessor().printHeaders(resp.getHeaders());
            return resp.getResponseBody();
        }
    }

    /**
     * @param resp will handle failed requests (not 2xx status codes response, except
     *             301 or 302).
     * @throws RequestFailedException at the end of this method whatever happens here.
     *                                This will be catched in the catch blocks of
     *                                {@link #getResponse()} method.
     */
    private void handleFailedRequest(Response resp) throws RequestFailedException {

        log.info("Failed Request with status code {} at url {}.", resp.getStatusCode(), url);
        httpManagerConfig.getBoundRequestBuilderProcessor().printReq(req);
        RequestFailedException e = new RequestFailedException(resp);
        httpManagerConfig.getExceptionLogger().logException(e, log.isTraceEnabled());
        throw e;
    }


    /**
     * <p>
     *     This method will retry the request and eventually perform routine tasks
     *     to count the number of tries, retry the connection process and reset the proxy.
     * </p>
     * @return the Stringified body of the retried request
     */
    private String retryRequest()
    {
        if(httpManagerConfig.useProxy())
        {
            actualProxy = RequestUtils.resetProxy(req, httpManagerConfig.getProxyFinder());
            if(httpManagerConfig.connectToProxyBeforeRequest() && httpConnectHandler != null)
                httpConnectHandler.tryConnect(url, actualProxy);

        }
        tries ++;
        return getResponse();
    }

}
