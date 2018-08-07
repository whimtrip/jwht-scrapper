package fr.whimtrip.ext.jwhtscrapper.service.scoped.req;

import fr.whimtrip.ext.jwhtscrapper.intfr.HttpMetrics;
import fr.whimtrip.ext.jwhtscrapper.service.base.RequestSynchronizer;
import fr.whimtrip.ext.jwhtscrapper.service.holder.DefaultHttpMetrics;
import fr.whimtrip.ext.jwhtscrapper.service.holder.HttpManagerConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * <p>
 *     Default Implementation of a {@link RequestSynchronizer}.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public final class RequestSynchronizerImpl implements RequestSynchronizer {


    private static final Logger log = LoggerFactory.getLogger(RequestSynchronizerImpl.class);

    private final HttpManagerConfig httpManagerConfig;
    private final AtomicBoolean scrapStopped;

    private final DefaultHttpMetrics defaultHttpMetrics;

    private Long lastRequest;

    private int lastProxyChange;


    /**
     * <p>Default constructor of this class. Features the httpManag</p>
     * @param httpManagerConfig the httpManagerConfig that will rule over this synchronizer.
     */
    public RequestSynchronizerImpl(@NotNull final HttpManagerConfig httpManagerConfig, @NotNull final AtomicBoolean scrapStopped) {
        this.lastRequest = System.currentTimeMillis() - httpManagerConfig.getAwaitBetweenRequests();
        this.lastProxyChange = httpManagerConfig.getProxyChangeRate();
        this.httpManagerConfig = httpManagerConfig;
        this.defaultHttpMetrics = new DefaultHttpMetrics();
        this.scrapStopped = scrapStopped;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void checkAwaitBetweenRequest(String url) {

        Long awaitedTime = System.currentTimeMillis() - lastRequest;
        lastRequest = System.currentTimeMillis();

        while(scrapStopped.get()) {

            try
            {
                Thread.sleep(1_000);
            }

            catch(InterruptedException e)
            {
                httpManagerConfig.getExceptionLogger().logException(e);
            }
        }

        if(awaitedTime < httpManagerConfig.getAwaitBetweenRequests())
        {
            log.info("Awaiting " + (httpManagerConfig.getAwaitBetweenRequests() - awaitedTime) + " ms");
            try{
                Thread.sleep(httpManagerConfig.getAwaitBetweenRequests() - awaitedTime);
                log.info("Awaited the  " + (httpManagerConfig.getAwaitBetweenRequests() - awaitedTime) + " ms");
            }
            catch(InterruptedException e){
                httpManagerConfig.getExceptionLogger().logException(e);
            }
        }

        log.info("Scrapping data at url " + url);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void incrementLastProxyChange() {
        lastProxyChange ++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getLastRequest() {

        return lastRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLastProxyChange() {

        return lastProxyChange;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logHttpStatus(int httpStatus, boolean newScrap) {
        defaultHttpMetrics.logHttpStatus(httpStatus, newScrap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpMetrics getHttpMetrics() {
        return defaultHttpMetrics;
    }
}
