package fr.whimtrip.ext.jwhtscrapper.service.scoped.req;

import fr.whimtrip.ext.jwhtscrapper.exception.RequestTimeoutException;
import fr.whimtrip.ext.jwhtscrapper.intfr.Proxy;
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
 * @author Louis-wht
 * @since 1.0.0
 */
public class HttpConnectHandler {

    private static final Logger log = LoggerFactory.getLogger(HttpConnectHandler.class);

    private final HttpManagerConfig httpManagerConfig;

    private final AsyncHttpClient asyncHttpClient;

    private final BoundRequestBuilder req;

    private int tries;

    private String url;


    public HttpConnectHandler(
            @NotNull final HttpManagerConfig httpManagerConfig,
            @NotNull final AsyncHttpClient asyncHttpClient,
            @NotNull final BoundRequestBuilder req,
            @NotNull final String url
    ){
        this.httpManagerConfig = httpManagerConfig;
        this.asyncHttpClient = asyncHttpClient;
        this.req = req;
        this.url = url;
    }

    public void tryConnect(String url) throws RequestTimeoutException
    {
        this.url = url;
        tryConnect();
    }

    public void tryConnect() throws RequestTimeoutException
    {
        boolean connected = false;
        Throwable actE = null;
        Proxy proxy =  null;
        try {
            BoundRequestBuilder connect = asyncHttpClient.prepareConnect(url);
            proxy = httpManagerConfig.getProxyFinder().findOneRandom();


            log.info(
                    String.format(
                            "Connecting to %s:%s in %s with method %s at url %s",
                            proxy.getIpAdress(),
                            proxy.getPort(),
                            proxy.getCountryName(),
                            httpManagerConfig.getBoundRequestBuilderProcessor().getMethod(req), url )
            );

            req.setProxyServer(proxy.getProxyServer());

            connect.setProxyServer(proxy.getProxyServer())
                    .execute()
                    .get(httpManagerConfig.getTimeout(), TimeUnit.MILLISECONDS);

            connected = true;
        }catch(IllegalArgumentException e)
        {
            actE = e;
            if(proxy != null)
                RequestUtils.setProxyStatus(proxy,Proxy.Status.BANNED, httpManagerConfig.getProxyFinder());
        }
        catch(InterruptedException | ExecutionException e)
        {
            actE = e;
            Throwable cause =  e.getCause();
            if(cause instanceof ConnectException)
            {
                if(proxy != null)
                    RequestUtils.setProxyStatus(proxy, Proxy.Status.BANNED, httpManagerConfig.getProxyFinder());
            }
        }
        catch(TimeoutException e )
        {
            actE = e;
            if(proxy != null)
                RequestUtils.setProxyStatus(proxy,Proxy.Status.FROZEN, httpManagerConfig.getProxyFinder());
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
