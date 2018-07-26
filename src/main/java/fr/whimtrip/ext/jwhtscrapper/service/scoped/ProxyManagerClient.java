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

import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.exception.ProxyRequestTimeoutException;
import fr.whimtrip.ext.jwhtscrapper.exception.RequestFailedException;
import fr.whimtrip.ext.jwhtscrapper.intfr.BasicObjectMapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.Proxy;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import fr.whimtrip.ext.jwhtscrapper.service.RotatingUserAgent;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import org.asynchttpclient.*;
import org.asynchttpclient.proxy.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by LOUISSTEIMBERG on 18/11/2017.
 */
public class ProxyManagerClient {

    private static final Logger log = LoggerFactory.getLogger(ProxyManagerClient.class);

    private static final String USER_AGENT_HEADER_NAME = "User-Agent";



    private HttpHeaders defaultHeaders;

    private final int awaitBetweenRequests;
    private final int proxyChangeRate;
    private final int timeout;
    private final boolean rotatingUserAgent;
    private final int maxRequestRetries;
    private final boolean useProxy;
    private final boolean connectToProxyBeforeRequest;
    private final boolean allowInfiniteRedirections;


    private final ExceptionLogger exceptionLogger;

    private final BasicObjectMapper objectMapper;

    private final ProxyFinder proxyFinder;

    private final BoundRequestBuilderProcessor boundRequestBuilderProcessor;



    private Long lastRequest;

    private int lastProxyChange;

    private List<Cookie> defaultCookies;

    private AsyncHttpClient asyncHttpClient;

    ProxyManagerClient(
            BasicObjectMapper objectMapper,
            ExceptionLogger exceptionLogger,
            AsyncHttpClient asyncHttpClient,
            ProxyFinder proxyFinder,
            BoundRequestBuilderProcessor boundRequestBuilderProcessor,
            int awaitBetweenRequests,
            int proxyChangeRate,
            int timeout,
            boolean useProxy,
            boolean connectToProxyBeforeRequest,
            boolean rotatingUserAgent,
            boolean allowInfiniteRedirections,
            int maxRequestRetries,
            HttpHeaders defaultHeaders,
            Cookie... defaultCookies
    ){

        this.objectMapper = objectMapper;
        this.exceptionLogger = exceptionLogger;
        this.asyncHttpClient = asyncHttpClient;
        this.proxyFinder = proxyFinder;
        this.boundRequestBuilderProcessor = boundRequestBuilderProcessor;
        this.awaitBetweenRequests = awaitBetweenRequests;
        this.proxyChangeRate = proxyChangeRate;
        this.timeout = timeout;
        this.rotatingUserAgent = rotatingUserAgent;
        this.allowInfiniteRedirections = allowInfiniteRedirections;
        this.maxRequestRetries = maxRequestRetries;
        this.connectToProxyBeforeRequest = connectToProxyBeforeRequest;
        this.lastRequest = System.currentTimeMillis() - awaitBetweenRequests;
        this.lastProxyChange = proxyChangeRate;
        this.useProxy = useProxy;

        this.defaultCookies = new ArrayList<>();
        this.defaultCookies.addAll(Arrays.asList(defaultCookies));

        this.defaultHeaders = new DefaultHttpHeaders();

        if(defaultHeaders != null)
            this.defaultHeaders.add(defaultHeaders);

        if(useProxy)
            buildNewAsyncClient();

    }


    // Prepare request
    public BoundRequestBuilder get(String url)
    {
        return prepareRequest(url, asyncHttpClient.prepareGet(url));
    }

    public BoundRequestBuilder post(String url)
    {
        return prepareRequest(url, asyncHttpClient.preparePost(url));
    }

    private void tryConnect(String url, BoundRequestBuilder req)
    {
        tryConnect(url, req, 0);
    }

    private void tryConnect(String url, BoundRequestBuilder req, int tries)
    {
        boolean connected = false;
        Throwable actE = null;
        Proxy proxy =  null;
        try {
            BoundRequestBuilder connect = asyncHttpClient.prepareConnect(url);
            proxy = proxyFinder.findOneRandom();


            log.info(
                    String.format(
                            "Connecting to %s:%s in %s with method %s at url %s",
                            proxy.getIpAdress(),
                            proxy.getPort(),
                            proxy.getCountryName(),
                            boundRequestBuilderProcessor.getMethod(req), url )
            );

            req.setProxyServer(proxy.getProxyServer());

            connect.setProxyServer(proxy.getProxyServer())
                    .execute()
                    .get(timeout, TimeUnit.MILLISECONDS);

            connected = true;
        }catch(IllegalArgumentException e)
        {
            actE = e;
            if(proxy != null)
                setProxyStatus(proxy,Proxy.Status.BANNED);
        }
        catch(InterruptedException | ExecutionException  e)
        {
            actE = e;
            Throwable cause =  e.getCause();
            if(cause instanceof ConnectException)
            {
                if(proxy != null)
                    setProxyStatus(proxy, Proxy.Status.BANNED);
            }
        }
        catch(TimeoutException e )
        {
            actE = e;
            if(proxy != null)
                setProxyStatus(proxy,Proxy.Status.FROZEN);
        }

        if(!connected)
        {

            exceptionLogger.logException(actE, false);

            if(tries >= maxRequestRetries)
                throw new ProxyRequestTimeoutException(actE);

            tryConnect(url, req, tries + 1);
        }
    }


    private BoundRequestBuilder prepareRequest(String url, BoundRequestBuilder req)
    {
        req.resetCookies();
        req.clearHeaders();
        req.setHeaders(buildDefaultHeaders());

        req.setCookies(defaultCookies);
        if(useProxy)
        {
            if(connectToProxyBeforeRequest)
                tryConnect(url, req);
            else
                setProxy(req);
        }
        return req;
    }

    private HttpHeaders buildDefaultHeaders() {

        HttpHeaders headers = boundRequestBuilderProcessor.newHttpHeaders(defaultHeaders);
        return rotatingUserAgent ?
                headers : headers.add(USER_AGENT_HEADER_NAME, RotatingUserAgent.pickRandomUserAgent());
    }

    private void setProxy(BoundRequestBuilder req) {
        Proxy proxy = proxyFinder.findOneRandom();
        req.setProxyServer(proxy.getProxyServer());
    }



    public <T> T extractModel(Class<T> modelType, BoundRequestBuilder req ) throws ProxyRequestTimeoutException, IOException
    {
        try {
            return objectMapper.readValue(
                    getResponse(req),
                    modelType
            );
        }

        catch(IOException e)
        {
            exceptionLogger.logException(e);
            throw e;
        }
    }

    public String getResponse(BoundRequestBuilder req)  throws ProxyRequestTimeoutException {
        return getResponse( req, 0);
    }

    public String getResponse(BoundRequestBuilder req, boolean followRedirections)  throws ProxyRequestTimeoutException {
        return getResponse( req, 0, false, followRedirections);
    }

    private String getResponse(BoundRequestBuilder req, int tries){
        return getResponse(req, tries, false);
    }

    private String getResponse(BoundRequestBuilder req, int tries, boolean redirection) {
        return getResponse(req, tries, redirection, true);
    }

    private String getResponse(BoundRequestBuilder req, int tries, boolean redirection, boolean followRedirections) {

        String response = "";

        Proxy actualProxy = boundRequestBuilderProcessor.getProxyServerFormRequestBuilder(req);

        String url = boundRequestBuilderProcessor.getUrlFromRequestBuilder(req);

        checkProxy(url);

        boolean requestIssued = false;
        Throwable actE = null;

        if(actualProxy == null) {
            log.info(String.format("Polling : with method %s at url %s",
                    boundRequestBuilderProcessor.getMethod(req),
                    url));
        }

        else{
            log.info(String.format("Polling : %s:%s in %s with method %s at url %s",
                    actualProxy.getIpAdress(), actualProxy.getPort(), actualProxy.getCountryName(),
                    boundRequestBuilderProcessor.getMethod(req), url));
        }

        try {

            Response resp = req.execute().get(timeout, TimeUnit.MILLISECONDS);

            log.info(String.format("Request status : %s %s", resp.getStatusCode(), resp.getStatusText()));

            if(resp.getStatusCode() == 301 || resp.getStatusCode() == 302
                    && resp.getHeader("Location") != null)
            {
                log.info("Trying to follow redirections to " + resp.getHeader("Location")
                        + " with followRedirections to " + followRedirections);
                if(followRedirections && (redirection || allowInfiniteRedirections))
                    return retryRequest(req.setUrl(resp.getHeader("Location")), tries + 1, true, followRedirections);
                else
                {
                    log.info("Get Redirected with body :  " + resp.getResponseBody());
                    boundRequestBuilderProcessor.printHeaders(resp.getHeaders());
                    return resp.getResponseBody();
                }
            }


            response = resp.getResponseBody();
            lastProxyChange ++;

            // if the status code is not 2xx successfull
            if(resp.getStatusCode() / 100 != 2)
            {
                boundRequestBuilderProcessor.printReq(req);
                RequestFailedException e = new RequestFailedException(resp);
                exceptionLogger.logException(e, false);
                throw e;
            }

            requestIssued = true;
        }
        catch(ProxyRequestTimeoutException | RequestFailedException e)
        {
            actE = e;
            if(useProxy && actualProxy != null) {
                setProxyStatus(actualProxy, Proxy.Status.BANNED);
                log.info(String.format("proxy %s:%s was banned", actualProxy.getId(), actualProxy.getPort()));
            }
        }
        catch(Exception e)
        {
            actE = e;
            lastProxyChange ++;
            if(useProxy && actualProxy != null )
            {
                if (actualProxy.getStatus() == Proxy.Status.FROZEN)
                {
                    setProxyStatus(actualProxy,Proxy.Status.BANNED);
                    log.info(String.format("proxy %s:%s was banned", actualProxy.getId(), actualProxy.getPort()));
                }
                else
                {
                    setProxyStatus(actualProxy,Proxy.Status.FROZEN);
                    log.info(String.format("proxy %s:%s was frozen", actualProxy.getId(), actualProxy.getPort()));
                }
            }
        }

        if(useProxy)
        {
            if (actualProxy != null && actualProxy.getStatus() == Proxy.Status.FROZEN)
            {
                setProxyStatus(actualProxy,Proxy.Status.WORKING);
            }
        }

        if(!requestIssued)
        {
            checkProxy(boundRequestBuilderProcessor.getUrlFromRequestBuilder(req));

            if(tries >= maxRequestRetries)
                throw new ProxyRequestTimeoutException(actE);

            return retryRequest(req, tries + 1, followRedirections);
        }

        return response;
    }

    private String retryRequest(BoundRequestBuilder req, int tries, boolean followRedirections) {
        return retryRequest(req, tries, false, followRedirections);
    }



    private String retryRequest(BoundRequestBuilder req, int tries, boolean redirection, boolean followRedirections)
    {
        if(useProxy)
        {
            if(connectToProxyBeforeRequest)
                tryConnect(boundRequestBuilderProcessor.getUrlFromRequestBuilder(req), req);
            else
                setProxy(req);
        }
        return getResponse(req, tries, redirection, followRedirections);
    }


    private synchronized void checkProxy(String url) {

        Long awaitedTime = System.currentTimeMillis() - lastRequest;
        lastRequest = System.currentTimeMillis();

        if(awaitedTime < awaitBetweenRequests)
        {
            log.info("Awaiting " + (awaitBetweenRequests - awaitedTime) + " ms");
            try{
                Thread.sleep(awaitBetweenRequests - awaitedTime);
                log.info("Awaited the  " + (awaitBetweenRequests - awaitedTime) + " ms");
            }
            catch(InterruptedException e){
                exceptionLogger.logException(e);
            }
        }

        log.info("Scrapping data at url " + url);
    }

    private void buildNewAsyncClient()
    {
        asyncHttpClient = new DefaultAsyncHttpClient(
                new DefaultAsyncHttpClientConfig.Builder()
                        .setProxyServer(new ProxyServer.Builder("127.0.0.1", 8888))
                        .build()
        );
    }


    private void setProxyStatus(Proxy proxy, Proxy.Status status) {
        if(proxy != null) {
            proxy.setStatus(status);
            proxyFinder.persistProxy(proxy);
        }
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

    public Long getLastRequest() {
        return lastRequest;
    }

    public int getLastProxyChange() {
        return lastProxyChange;
    }



}
