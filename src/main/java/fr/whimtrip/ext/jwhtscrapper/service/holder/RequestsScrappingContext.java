/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service.holder;

import fr.whimtrip.ext.jwhtscrapper.annotation.Link;
import fr.whimtrip.ext.jwhtscrapper.annotation.RequestsConfig;
import fr.whimtrip.ext.jwhtscrapper.annotation.Scrapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.HtmlAutoScrapper;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     This class is a holder for the request scrapping context.
 *     This context will hold the required parameters necessary for
 *     {@link HtmlAutoScrapper} to run.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class RequestsScrappingContext {

    private RequestsConfig requestsConfig;

    private Integer parrallelThreads, scrapLimit;

    private Link.Method method;

    private boolean throwExceptions;

    private Class<?> modelClass;


    /**
     * @param scrapper {@link Scrapper} annotation on top of the {@link ScrapperHelper}.
     *                 can provide all required information to create the current
     *                 {@link RequestsScrappingContext}.
     */
    public RequestsScrappingContext(Scrapper scrapper) {
        this(
                scrapper.requestConfig(),
                scrapper.requestConfig().parallelThreads(),
                scrapper.scrapLimit(),
                scrapper.method(),
                scrapper.throwExceptions(),
                scrapper.scrapModel()
        );
    }

    /**
     *
     * @param requestsConfig the {@link RequestsConfig} annotation normally included
     *                       in the {@link Scrapper} annotation of the {@link ScrapperHelper}
     * @param parrallelThreads the number of parrallel threads to use concurrently to run
     *                         the scraps.
     * @param scrapLimit the scrapping limit in terms of entry web pages to scrap / scraps
     *                   threads to start.
     * @param method the HTTP method to use. currently, only GET and POST are supported.
     * @param throwExceptions wether exceptions catched at the scrapper level should be
     *                        catched and logged or thrown (in which case it will stop the
     *                        whole scrapping process and close current running tasks at
     *                        the first exception encountered).
     * @param modelClass the class to use to map resulting objects to.
     */
    public RequestsScrappingContext(
            @NotNull final RequestsConfig requestsConfig,
            @NotNull final Integer parrallelThreads,
            @NotNull final Integer scrapLimit,
            @NotNull final Link.Method method,
                     final boolean throwExceptions,
            @NotNull final Class<?> modelClass
    ){
        this.requestsConfig = requestsConfig;
        this.parrallelThreads = parrallelThreads;
        this.scrapLimit = scrapLimit;
        this.method = method;
        this.throwExceptions = throwExceptions;
        this.modelClass = modelClass;
    }

    /**
     *
     * @return the {@link RequestsConfig} annotation normally included in the {@link Scrapper}
     *         annotation of the {@link ScrapperHelper}. It contains informations about how
     *         requests should be performed with a lot of configurations to allow some fine
     *         grained scrapping requesting operations.
     */
    public RequestsConfig getRequestsConfig() {
        return requestsConfig;
    }

    /**
     * @return the number of parrallel threads to use concurrently to run the scraps.
     */
    public Integer getParrallelThreads() {
        return parrallelThreads;
    }

    /**
     * @return the scrapping limit in terms of entry web pages to scrap / scraps threads to start.
     */
    public Integer getScrapLimit() {
        return scrapLimit;
    }

    /**
     * @see Link.Method
     * @return the HTTP method to use.
     */
    public Link.Method getMethod() {
        return method;
    }

    /**
     * @return wether exceptions catched at the scrapper level should be catched and logged or
     * thrown (in which case it will stop the whole scrapping process and close current running
     * tasks at the first exception encountered).
     */
    public boolean isThrowExceptions() {
        return throwExceptions;
    }

    /**
     * @return the class to use to map resulting objects to.
     */
    public Class getModelClass() {
        return modelClass;
    }

}
