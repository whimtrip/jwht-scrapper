/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */


package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;
import fr.whimtrip.ext.jwhtscrapper.service.base.AutomaticScrapperClient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 *
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     A Scrapping Context will hold required informations to
 *     process to the {@link AutomaticScrapperClient}:
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 * @param <P> P is the parent object which will be used each time to create the request and that
 *           in the end will be modified.
 * @param <M> M is the model on which the Html responses will be mapped
 * @param <H> H is the Scrapper Helper instance that will be used to interpole some code in
 *           the different steps of the scrapping task.
 */
public class ScrappingContext<P, M,  H extends ScrapperHelper<P, M>> {

    private final List<P> parentObjects;
    private final Class<P> parentClazz;
    private final Class<M> modelClazz;
    private final H helper;
    private final RequestScrappingContext requestScrappingContext;


    @SuppressWarnings("unchecked")
    public ScrappingContext(
            @NotNull final List<P> parentObjects,
            @NotNull final Class<P> parentClazz,
            @NotNull final H helper
    ){

        this.parentObjects = parentObjects;
        this.parentClazz = parentClazz;
        this.helper = helper;
        this.requestScrappingContext = helper.init();
        this.modelClazz = requestScrappingContext.getModelClass();
    }

    /**
     * @return the list of parentObjects of the scrapping
     *         context.
     */
    public List<P> getParentObjects() {

        return parentObjects;
    }

    /**
     * @return the parent Clazz.
     */
    public Class<P> getParentClazz() {
        return parentClazz;
    }

    /**
     * @return the model clazz on which scrapping output will be mapped.
     */
    public Class<M> getModelClazz() {
        return modelClazz;
    }

    /**
     * @return the helper class that will handle all of the hooks and
     *         object custom processing.
     */
    public H getHelper() {
        return helper;
    }

    /**
     * @return the scrapping context of this request which holds
     *         information that will be used by the {@link HtmlAutoScrapper}
     *         and the {@link ProxyManagerClient}.
     */
    public RequestScrappingContext getRequestScrappingContext() {
        return requestScrappingContext;
    }

}