/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */


package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;

/**
 *
 * @param <C> Object that contains the P objects. This object is either a List< P > or P itself
 * @param <P> P is the parent object which will be used each time to create the request and that
 *           in the end will be modified.
 * @param <M> M is the model on which the Html responses will be mapped
 * @param <H> H is the Scrapper Helper instance that will be used to interpole some code in
 *           the different steps of the scrapping task.
 */
public class ScrappingContext<C, P, M,  H extends ScrapperHelper<P, M>> {

    C containerObject;
    Class<P> parentClazz;
    Class<M> modelClazz;
    H helper;
    RequestScrappingContext requestScrappingContext;
    String name;


    public C getContainerObject() {
        return containerObject;
    }

    public ScrappingContext setContainerObject(C containerObject) {
        this.containerObject = containerObject;
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

    public String getName() {
        return name;
    }

    public ScrappingContext setName(String name) {

        this.name = name;
        return this;
    }
}