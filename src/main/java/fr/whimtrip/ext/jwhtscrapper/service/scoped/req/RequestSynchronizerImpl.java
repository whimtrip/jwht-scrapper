package fr.whimtrip.ext.jwhtscrapper.service.scoped.req;

import fr.whimtrip.ext.jwhtscrapper.intfr.HttpMetrics;
import fr.whimtrip.ext.jwhtscrapper.service.base.RequestSynchronizer;
import fr.whimtrip.ext.jwhtscrapper.service.holder.DefaultHttpMetrics;
import fr.whimtrip.ext.jwhtscrapper.service.holder.HttpManagerConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class RequestSynchronizerImpl implements RequestSynchronizer {


    private static final Logger log = LoggerFactory.getLogger(RequestSynchronizerImpl.class);

    private final HttpManagerConfig httpManagerConfig;

    private final DefaultHttpMetrics defaultHttpMetrics;

    private Long lastRequest;

    private int lastProxyChange;


    /**
     * <p>Default constructor of this class. Features the httpManag</p>
     * @param httpManagerConfig the httpManagerConfig that will rule over this synchronizer.
     */
    public RequestSynchronizerImpl(@NotNull final HttpManagerConfig httpManagerConfig) {
        this.lastRequest = System.currentTimeMillis() - httpManagerConfig.getAwaitBetweenRequests();
        this.lastProxyChange = httpManagerConfig.getProxyChangeRate();
        this.httpManagerConfig = httpManagerConfig;
        this.defaultHttpMetrics = new DefaultHttpMetrics();
    }


    /**
     * @param url see {@link RequestSynchronizer#checkAwaitBetweenRequest(String)}
     */
    @Override
    public synchronized void checkAwaitBetweenRequest(String url) {

        Long awaitedTime = System.currentTimeMillis() - lastRequest;
        lastRequest = System.currentTimeMillis();

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
     * @see RequestSynchronizer#incrementLastProxyChange()
     */
    @Override
    public synchronized void incrementLastProxyChange() {
        lastProxyChange ++;
    }

    /**
     * @return see {@link RequestSynchronizer#getLastRequest()}
     */
    @Override
    public Long getLastRequest() {

        return lastRequest;
    }

    /**
     * @return see {@link RequestSynchronizer#getLastProxyChange()}
     */
    @Override
    public int getLastProxyChange() {

        return lastProxyChange;
    }

    /**
     * @param httpStatus see {@link RequestSynchronizer#logHttpStatus(int, boolean)}
     * @param newScrap see {@link RequestSynchronizer#logHttpStatus(int, boolean)}
     *
     */
    @Override
    public void logHttpStatus(int httpStatus, boolean newScrap) {
        defaultHttpMetrics.logHttpStatus(httpStatus, newScrap);
    }

    /**
     * @return see {@link RequestSynchronizer#getHttpMetrics()}
     */
    @Override
    public HttpMetrics getHttpMetrics() {
        return defaultHttpMetrics;
    }
}
