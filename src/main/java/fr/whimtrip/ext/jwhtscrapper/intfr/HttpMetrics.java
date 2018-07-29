package fr.whimtrip.ext.jwhtscrapper.intfr;

import fr.whimtrip.ext.jwhtscrapper.service.holder.HttpManagerConfig;
import fr.whimtrip.ext.jwhtscrapper.service.holder.StatusRange;

import java.util.Map;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     Interface defining the contracts that any implementing metrics
 *     class should follow.
 * </p>
 *
 * <p>
 *     Compared to {@link ScrappingStats}, this features much lower level
 *     stats about HTTP requests performed under the hood in hidden processing
 *     units.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface HttpMetrics {


    /**
     * @param statusCode the HTTP status code to find stats for.
     * @return the number of HTTP responses received with this HTTP status code.
     */
    int getStatusCount(int statusCode);

    /**
     * @param statusCode the HTTP status code to find stats for.
     * @return the percentage of HTTP responses received with this HTTP status code
     *         among all other HTTP responses.
     */
    float getStatusPercentage(int statusCode);

    /**
     * @param statusRange the HTTP status code range to find stats for.
     * @return the number of HTTP responses received with HTTP status codes
     *         within this status range count.
     */
    int getStatusRangeCount(StatusRange statusRange);

    /**
     * @param statusRange the HTTP status code range to find stats for.
     * @return the percentage of HTTP responses received with HTTP status codes
     *         within this status range count among all other HTTP responses.
     */
    float getStatusRangePercentage(StatusRange statusRange);

    /**
     * @return the total number of HTTP requests made.
     */
    int getTotalHttpRequestsMade();

    /**
     * @return the total number of scraps performed. As one scrap may fail up
     *         to {@link HttpManagerConfig#getMaxRequestRetries()}, this value
     *         should be far lower than the {@link #getTotalHttpRequestsMade()}
     *         but cannot be greater in any case.
     */
    int getTotalScrapsPerformed();

    /**
     * @return the map of HTTP statuses codes with their respective count of
     *         occurences.
     */
    Map<Integer, Integer> getStatusesMap();

    /**
     * @return the map of HTTP statuses code ranges with their respective count of
     *         occurences.
     */
    Map<StatusRange, Integer> getStatusesRangeMap();
}
