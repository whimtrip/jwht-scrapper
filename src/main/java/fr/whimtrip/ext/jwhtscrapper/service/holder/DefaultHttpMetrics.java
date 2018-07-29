package fr.whimtrip.ext.jwhtscrapper.service.holder;

import fr.whimtrip.ext.jwhtscrapper.impl.ScrappingStatsImpl;
import fr.whimtrip.ext.jwhtscrapper.intfr.HttpMetrics;
import fr.whimtrip.ext.jwhtscrapper.service.base.RequestSynchronizer;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.req.RequestCoreHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     Default Implementation of {@link HttpMetrics}. This implementation
 *     auto update itself and will always be the same for a single
 *     {@link RequestCoreHandler}. This in memory implementation type
 *     differs a lot from {@link ScrappingStatsImpl} which never gets
 *     auto updated and needs to be gathered each time the stats are queried.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class DefaultHttpMetrics implements HttpMetrics {

    private final Map<StatusRange, Integer> statusRangeMap = new HashMap<>(StatusRange.values().length);

    private final Map<Integer, Integer> statusesMap = new HashMap<>();

    private final AtomicInteger totalScrapsCount = new AtomicInteger(0);
    private final AtomicInteger totalRequestsCount = new AtomicInteger(0);

    /**
     * Default constructor that will simply populate the inner {@link #statusRangeMap}
     * with the possible values it can take.
     */
    public DefaultHttpMetrics() {
        for(StatusRange sr : StatusRange.values()) {
            statusRangeMap.put(sr, 0);
        }
    }

    /**
     * @param statusCode see {@link HttpMetrics#getStatusCount(int)}
     * @return see {@link HttpMetrics#getStatusCount(int)}
     */
    @Override
    public int getStatusCount(int statusCode) {
        return statusesMap.getOrDefault(statusCode, 0);
    }

    /**
     * @param statusCode see {@link HttpMetrics#getStatusPercentage(int)}
     * @return see {@link HttpMetrics#getStatusPercentage(int)}
     */
    @Override
    public float getStatusPercentage(int statusCode) {
        return extractPercentage( getStatusCount(statusCode) );
    }

    /**
     * @param statusRange see {@link HttpMetrics#getStatusRangeCount(StatusRange)}.
     * @return see {@link HttpMetrics#getStatusRangeCount(StatusRange)}
     */
    @Override
    public int getStatusRangeCount(StatusRange statusRange) {
        return statusRangeMap.get(statusRange);
    }

    /**
     * @param statusRange see {@link HttpMetrics#getStatusRangePercentage(StatusRange)}
     * @return see {@link HttpMetrics#getStatusRangePercentage(StatusRange)}
     */
    @Override
    public float getStatusRangePercentage(StatusRange statusRange) {
        return extractPercentage( getStatusRangeCount(statusRange) );
    }

    /**
     * @return see {@link HttpMetrics#getTotalHttpRequestsMade()}
     */
    @Override
    public int getTotalHttpRequestsMade() {
        return totalRequestsCount.get();
    }

    /**
     * @return see {@link HttpMetrics#getTotalScrapsPerformed()}
     */
    @Override
    public int getTotalScrapsPerformed() {
        return totalScrapsCount.get();
    }

    /**
     * @return see {@link HttpMetrics#getStatusesMap()}
     */
    @Override
    public Map<Integer, Integer> getStatusesMap() {
        synchronized (statusesMap) {
            return statusesMap;
        }
    }

    /**
     * @return see {@link HttpMetrics#getStatusesRangeMap()}
     */
    @Override
    public Map<StatusRange, Integer> getStatusesRangeMap() {
        synchronized (statusRangeMap) {
            return statusRangeMap;
        }
    }


    /**
     * <p>
     *     Use in this implementation to delegate
     *     {@link RequestSynchronizer#logHttpStatus(int, boolean)}
     *     to the metrics holder directly to ensure direct and live
     *     update of the metrics in the same instance for memory and
     *     processing efficiency.
     * </p>
     * @param httpStatus the httpStatus to log
     * @param newScrap wether it is a new scrap (first try) or a retry
     *                 request.
     */
    public void logHttpStatus(int httpStatus, boolean newScrap) {

        synchronized (statusRangeMap) {
            StatusRange statusRange = StatusRange.getStatusRange(httpStatus);
            Integer reqCount = statusRangeMap.get(statusRange);
            statusRangeMap.put(statusRange, reqCount + 1);
        }

        synchronized (statusesMap) {
            Integer reqCount = statusesMap.getOrDefault(httpStatus, 0);
            statusesMap.put(httpStatus, reqCount + 1);
        }

        totalRequestsCount.incrementAndGet();

        if(newScrap) totalScrapsCount.incrementAndGet();
    }


    /**
     * @param partialReqCount the count of request receiving a certain type
     *                        of HTTP status codes responses.
     * @return the extracted percentage out of this partial request count.
     */
    private float extractPercentage(int partialReqCount) {
        return (float) ( partialReqCount * 100 ) / totalRequestsCount.get();
    }
}
