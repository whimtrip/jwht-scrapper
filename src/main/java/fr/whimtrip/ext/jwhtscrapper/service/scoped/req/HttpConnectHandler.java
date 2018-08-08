package fr.whimtrip.ext.jwhtscrapper.service.scoped.req;

import fr.whimtrip.ext.jwhtscrapper.enm.Status;
import fr.whimtrip.ext.jwhtscrapper.exception.RequestTimeoutException;
import fr.whimtrip.ext.jwhtscrapper.intfr.Proxy;
import fr.whimtrip.ext.jwhtscrapper.service.base.RequestSynchronizer;
import fr.whimtrip.ext.jwhtscrapper.service.holder.HttpManagerConfig;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * <p>
 *     This class will handle pre-proxy connection when proxies are used
 *     an {@link HttpManagerConfig#connectToProxyBeforeRequest()} is enabled.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public final class HttpConnectHandler {

    private static final Logger log = LoggerFactory.getLogger(HttpConnectHandler.class);

    private final HttpManagerConfig httpManagerConfig;

    private final AsyncHttpClient asyncHttpClient;

    private final RequestSynchronizer requestSynchronizer;

    private final BoundRequestBuilder req;

    private Proxy proxy;

    private int tries;

    private String url;


    /**
     * <p>Default constructor for this handler class.</p>
     * @param httpManagerConfig the http config holder
     * @param asyncHttpClient the http client to use to perform all HTTP
     *                        requests and CONNECT TCP operations.
     * @param requestSynchronizer the request syncer. see {@link RequestSynchronizer}
     * @param req the prepared request to try out.
     * @param url the url to connect to.
     * @param proxy the proxy to use to test the connection.
     */
    public HttpConnectHandler(
            @NotNull final HttpManagerConfig httpManagerConfig,
            @NotNull final AsyncHttpClient asyncHttpClient,
            @NotNull final RequestSynchronizer requestSynchronizer,
            @NotNull final BoundRequestBuilder req,
            @NotNull final String url,
            @NotNull final Proxy proxy
    ){
        this.httpManagerConfig = httpManagerConfig;
        this.asyncHttpClient = asyncHttpClient;
        this.requestSynchronizer = requestSynchronizer;
        this.req = req;
        this.url = url;
        this.proxy = proxy;
    }

    /**
     * <p>
     *     When both the url and proxy has changed since first time call,
     *     this method should be called instead of {@link #tryConnect()}.
     * </p>
     * @param url the new url to try a connection at
     * @param proxy the new proxy to try a connection at
     * @throws RequestTimeoutException if the Connect Request timed out.
     */
    public void tryConnect(String url, Proxy proxy) throws RequestTimeoutException
    {
        this.url = url;
        this.proxy = proxy;
        tryConnect();
    }

    /**
     * <p>Inner connect processing.</p>
     * @throws RequestTimeoutException if the connections timed out. This will
     *                                 also freeze / ban the current proxy.
     */
    public void tryConnect() throws RequestTimeoutException
    {
        boolean connected = false;
        Throwable actE = null;

        try {
            requestSynchronizer.checkAwaitBetweenRequest(url);
            BoundRequestBuilder connect = asyncHttpClient.prepareConnect(url);

            if(log.isDebugEnabled())
                log.debug(
                        String.format(
                                "Connecting to %s:%s in %s with method %s at url %s",
                                proxy.getIpAdress(),
                                proxy.getPort(),
                                proxy.getCountryName(),
                                httpManagerConfig.getBoundRequestBuilderProcessor().getMethod(req), url )
                );

            connect .execute()
                    .get(httpManagerConfig.getTimeout(), TimeUnit.MILLISECONDS);

            connected = true;
        }catch(IllegalArgumentException e)
        {
            actE = e;
            if(proxy != null)
                RequestUtils.setProxyStatus(proxy, Status.BANNED, httpManagerConfig.getProxyFinder());
        }
        catch(InterruptedException | ExecutionException e)
        {
            actE = e;
            Throwable cause =  e.getCause();
            if(cause instanceof ConnectException)
            {
                if(proxy != null)
                    RequestUtils.setProxyStatus(proxy, Status.BANNED, httpManagerConfig.getProxyFinder());
            }
        }
        catch(TimeoutException e )
        {
            actE = e;
            if(proxy != null)
                RequestUtils.setProxyStatus(proxy, Status.FROZEN, httpManagerConfig.getProxyFinder());
        }

        if(!connected)
        {

            httpManagerConfig.getExceptionLogger().logException(actE, false);

            if(tries >= httpManagerConfig.getMaxRequestRetries())
                throw new RequestTimeoutException(actE);

            tries ++;
            tryConnect();
        }
    }
}
