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

import fr.whimtrip.ext.jwhtscrapper.annotation.Link;
import fr.whimtrip.ext.jwhtscrapper.annotation.RequestsConfig;
import fr.whimtrip.ext.jwhtscrapper.annotation.Scrapper;

/**
 * Created by LOUISSTEIMBERG on 22/11/2017.
 */
public class RequestScrappingContext {

    private RequestsConfig requestsConfig;

    private Integer parrallelThreads, scrapLimit;

    private Link.Method method;

    private boolean throwExceptions;

    private Class<?> modelClass;


    public static RequestScrappingContext build(Scrapper scrapper){
        RequestsConfig requestsConfig = scrapper.requestConfig();
        return new RequestScrappingContext()
                .setRequestsConfig(requestsConfig)
                .setParrallelThreads(requestsConfig.parallelThreads())
                .setScrapLimit(scrapper.scrapLimit())
                .setMethod(scrapper.method())
                .setThrowExceptions(scrapper.throwExceptions())
                .setModelClass(scrapper.scrapModel());
    }

    public RequestsConfig getRequestsConfig() {
        return requestsConfig;
    }

    private RequestScrappingContext setRequestsConfig(RequestsConfig requestsConfig) {
        this.requestsConfig = requestsConfig;
        return this;
    }

    public Integer getParrallelThreads() {
        return parrallelThreads;
    }

    private RequestScrappingContext setParrallelThreads(Integer parrallelThreads) {
        this.parrallelThreads = parrallelThreads;
        return this;
    }

    public Integer getScrapLimit() {
        return scrapLimit;
    }

    private RequestScrappingContext setScrapLimit(Integer scrapLimit) {
        this.scrapLimit = scrapLimit;
        return this;
    }

    public Link.Method getMethod() {
        return method;
    }

    private RequestScrappingContext setMethod(Link.Method method) {
        this.method = method;
        return this;
    }

    public boolean isThrowExceptions() {
        return throwExceptions;
    }

    private RequestScrappingContext setThrowExceptions(boolean throwExceptions) {
        this.throwExceptions = throwExceptions;
        return this;
    }

    public Class getModelClass() {
        return modelClass;
    }

    private RequestScrappingContext setModelClass(Class modelClass) {
        this.modelClass = modelClass;
        return this;
    }
}
