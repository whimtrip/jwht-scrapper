/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */


package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;

import java.util.List;

/**
 *
 * @param <P> P is the parent object which will be used each time to create the request and that
 *           in the end will be modified.
 * @param <M> M is the model on which the Html responses will be mapped
 * @param <H> H is the Scrapper Helper instance that will be used to interpole some code in
 *           the different steps of the scrapping task.
 */
public class ScrappingContext<P, M,  H extends ScrapperHelper<P, M>> {

    List<P> parentObjects;
    Class<P> parentClazz;
    Class<M> modelClazz;
    H helper;
    RequestScrappingContext requestScrappingContext;


    public List<P> getParentObjects() {

        return parentObjects;
    }

    public ScrappingContext setParentObjects(List<P> parentObjects) {

        this.parentObjects = parentObjects;
        return this;
    }

    public Class<P> getParentClazz() {
        return parentClazz;
    }

    public ScrappingContext setParentClazz(Class<P> parentClazz) {
        this.parentClazz = parentClazz;
        return this;
    }

    public Class<M> getModelClazz() {
        return modelClazz;
    }

    public ScrappingContext setModelClazz(Class<M> modelClazz) {
        this.modelClazz = modelClazz;
        return this;
    }

    public H getHelper() {
        return helper;
    }

    public ScrappingContext setHelper(H helper) {
        this.helper = helper;
        return this;
    }

    public RequestScrappingContext getRequestScrappingContext() {
        return requestScrappingContext;
    }

    public ScrappingContext setRequestScrappingContext(RequestScrappingContext requestScrappingContext) {
        this.requestScrappingContext = requestScrappingContext;
        return this;
    }

}