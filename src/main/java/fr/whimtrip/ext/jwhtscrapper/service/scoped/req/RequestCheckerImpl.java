package fr.whimtrip.ext.jwhtscrapper.service.scoped.req;

import fr.whimtrip.ext.jwhtscrapper.service.base.RequestChecker;
import fr.whimtrip.ext.jwhtscrapper.service.holder.HttpManagerConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class RequestCheckerImpl implements RequestChecker {


    private static final Logger log = LoggerFactory.getLogger(RequestCheckerImpl.class);

    private final HttpManagerConfig httpManagerConfig;


    private Long lastRequest;

    private int lastProxyChange;



    public RequestCheckerImpl(int proxyChangeRate, long awaitBetweenRequests, @NotNull final HttpManagerConfig httpManagerConfig) {
        this.lastRequest = System.currentTimeMillis() - awaitBetweenRequests;
        this.lastProxyChange = proxyChangeRate;
        this.httpManagerConfig = httpManagerConfig;
    }


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


    @Override
    public synchronized void incrementLastProxyChange() {
        lastProxyChange ++;
    }

    public Long getLastRequest() {

        return lastRequest;
    }

    public int getLastProxyChange() {

        return lastProxyChange;
    }
}
