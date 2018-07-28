package fr.whimtrip.ext.jwhtscrapper.service.scoped.req;

import fr.whimtrip.ext.jwhtscrapper.exception.RequestTimeoutException;
import fr.whimtrip.ext.jwhtscrapper.exception.RequestFailedException;
import fr.whimtrip.ext.jwhtscrapper.intfr.Proxy;
import fr.whimtrip.ext.jwhtscrapper.service.base.RequestChecker;
import fr.whimtrip.ext.jwhtscrapper.service.holder.HttpManagerConfig;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class RequestCoreHandler {


    private static final Logger log = LoggerFactory.getLogger(RequestCoreHandler.class);

    private final HttpManagerConfig httpManagerConfig;

    private final RequestChecker requestChecker;

    private final HttpConnectHandler httpConnectHandler;

    private final BoundRequestBuilder req;

    private final boolean followRedirections;

    private Proxy actualProxy;

    private boolean firstRedirection = true;

    private int tries = 0;

    private String url;




    public RequestCoreHandler(
            @NotNull  final HttpManagerConfig httpManagerConfig,
            @NotNull  final RequestChecker requestChecker,
            @Nullable final HttpConnectHandler httpConnectHandler,
            @NotNull  final BoundRequestBuilder req,
            @NotNull  final String url,
                      final boolean followRedirections
    ){

        this.httpManagerConfig = httpManagerConfig;
        this.requestChecker = requestChecker;
        this.httpConnectHandler = httpConnectHandler;
        this.req = req;
        this.followRedirections = followRedirections;
        this.url = url;
        this.actualProxy = httpManagerConfig.getBoundRequestBuilderProcessor().getProxyServerFromRequestBuilder(req);
    }

    public String getResponse() {

        requestChecker.checkAwaitBetweenRequest(url);

        boolean requestIssued = false;
        String response = "";
        Throwable actE = null;

        if(log.isDebugEnabled()) logRequest();

        try {

            Response resp = req.execute().get(httpManagerConfig.getTimeout(), TimeUnit.MILLISECONDS);

            int statusCode = resp.getStatusCode();

            log.debug("Request status : {} {}", statusCode, resp.getStatusText());

            if((statusCode == 301 || statusCode == 302) && resp.getHeader("Location") != null)
                return handleRedirection(resp);

            response = resp.getResponseBody();
            requestChecker.incrementLastProxyChange();

            // if the status code is not 2xx successfull
            if(statusCode / 100 != 2) return handleFailedRequest(resp);

            requestIssued = true;
        }
        catch(RequestTimeoutException | RequestFailedException e)
        {
            actE = e;
            handleRequestException();
        }
        catch(Exception e)
        {
            actE = e;
            handleUnknownException();
        }

        if(httpManagerConfig.useProxy() && requestIssued) unfreezeProxy();
        if(!requestIssued) return retryRequest(actE);

        return response;
    }

    private void unfreezeProxy() {

        if (actualProxy != null && actualProxy.getStatus() == Proxy.Status.FROZEN)
        {
            RequestUtils.setProxyStatus(actualProxy,Proxy.Status.WORKING, httpManagerConfig.getProxyFinder());
        }
    }

    private String retryRequest(Throwable actE) {

        requestChecker.checkAwaitBetweenRequest(httpManagerConfig.getBoundRequestBuilderProcessor().getUrlFromRequestBuilder(req));

        if(tries >= httpManagerConfig.getMaxRequestRetries())
            throw new RequestTimeoutException(actE);

        return retryRequest();
    }

    private void handleRequestException() {

        if(httpManagerConfig.useProxy() && actualProxy != null) {
            RequestUtils.setProxyStatus(actualProxy, Proxy.Status.BANNED, httpManagerConfig.getProxyFinder());
            log.info("Proxy {}:{} was banned", actualProxy.getId(), actualProxy.getPort());
        }
    }

    private void handleUnknownException() {

        requestChecker.incrementLastProxyChange();
        if(httpManagerConfig.useProxy() && actualProxy != null )
        {
            if (actualProxy.getStatus() == Proxy.Status.FROZEN)
            {
                RequestUtils.setProxyStatus(actualProxy,Proxy.Status.BANNED, httpManagerConfig.getProxyFinder());
                log.info("Proxy {}:{} was banned", actualProxy.getId(), actualProxy.getPort());
            }
            else
            {
                RequestUtils.setProxyStatus(actualProxy,Proxy.Status.FROZEN, httpManagerConfig.getProxyFinder());
                log.info("Proxy {}:{} was frozen", actualProxy.getId(), actualProxy.getPort());
            }
        }
    }

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

    private String handleRedirection(Response resp) {

        url = resp.getHeader("Location");
        log.info("Trying to follow redirections to {} with followRedirections to {}.", url, followRedirections);
        if(followRedirections && (firstRedirection || httpManagerConfig.allowInfiniteRedirections())) {
            req.setUrl(url);
            firstRedirection = false;
            return retryRequest();
        }
        else
        {
            log.info("Get Redirected with body :  {}.", resp.getResponseBody());
            httpManagerConfig.getBoundRequestBuilderProcessor().printHeaders(resp.getHeaders());
            return resp.getResponseBody();
        }
    }

    private String handleFailedRequest(Response resp) {

        log.info("Failed Request with status code {} at url {}.", resp.getStatusCode(), url);
        httpManagerConfig.getBoundRequestBuilderProcessor().printReq(req);
        RequestFailedException e = new RequestFailedException(resp);
        httpManagerConfig.getExceptionLogger().logException(e, log.isTraceEnabled());
        throw e;
    }


    private String retryRequest()
    {
        if(httpManagerConfig.useProxy())
        {
            if(httpManagerConfig.connectToProxyBeforeRequest() && httpConnectHandler != null)
                httpConnectHandler.tryConnect(url);
            else
                actualProxy = RequestUtils.resetProxy(req, httpManagerConfig.getProxyFinder());
        }
        tries ++;
        return getResponse();
    }

}
