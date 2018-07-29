/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.intfr;

import fr.whimtrip.core.util.exception.ObjectCreationException;
import fr.whimtrip.ext.jwhtscrapper.annotation.Scrapper;
import fr.whimtrip.ext.jwhtscrapper.exception.ModelBindingException;
import fr.whimtrip.ext.jwhtscrapper.service.base.AutomaticScrapperClient;
import fr.whimtrip.ext.jwhtscrapper.service.holder.RequestsScrappingContext;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;


/**
 *
 * @param <P> Parent class which will hold the modifications
 * @param <M> M contains the model on which the html scrapping will be mapped.
 */
public interface ScrapperHelper<P, M> {


    /**
     * readAndSave method. It can be used to retrieve vars from the builder, etc...
     * @return initialized {@link RequestsScrappingContext}
     */
    default RequestsScrappingContext init() throws ObjectCreationException {
        Scrapper scrapper = this.getClass().getAnnotation(Scrapper.class);

        if(scrapper == null)
            throw new ObjectCreationException(this.getClass() + ". Classes implementing ScrapperHelper must have an @Scrapper annotation.");


        return new RequestsScrappingContext(scrapper);
    }

    /**
     * Called at the beginning of the process after readAndSave, this will try to
     * consider wether this entity should be scraped or not
     * @param parent the parent object
     * @return a boolean indicating wether this parent object should or shouldn't
     *         be scrapped.
     */
    boolean shouldBeScrapped(@NotNull final P parent);

    /**
     * Return the url of the targeted web page
     * @param parent the parent object used to perform the actual scrapping operation
     * @return the created url.
     * @throws URISyntaxException TODO change that one
     */
    String createUrl(@NotNull final P parent) throws URISyntaxException;

    /**
     * Edit the actual request before it will be sent to network
     * @param req the request to edit
     * @param parent the parent object
     * @return the edited request. It must be the same request instance provided
     *         in the input parameter.
     */
    void editRequest(@NotNull final BoundRequestBuilder req, @NotNull final P parent);


    /**
     * Edit and instanciate the model before requests will be done. This will typically be used in order to inject
     * properties into the model so that it can be further injected into lower levels of the models
     * to make for example a decision on wether a link should be followed or not
     * @param parent the parent object
     * @return the newly instanciated model.
     */
    M instanciateModel(@NotNull final P parent);

    /**
     * Edit the object using the model mapped out of the scrapper job
     * @param parent the parent object
     * @param model the model created by the scrapping operation
     */
    void buildModel(@NotNull final P parent, @NotNull final M model) throws ModelBindingException;

    /**
     * Decide wether or not those modifications should be saved
     * @param parent the parent entity
     * @param model the resulting model
     * @return a boolean indicating wether you should save or not the entity
     */
    boolean shouldBeSaved(@NotNull final P parent, final M model);


    /**
     * Save / Persist the resulting entity here / resulting Model, in a database for example.
     * @param parentObject the parent object (you might want to persist one of them, both or even none)
     * @param model the model resulting of the scrapping process, might or might not be persisted depending
     *              on the use case.
     */
    void save(@NotNull final P parentObject, final M model);

    /**
     * Return an object to the origin service so that it could be eventually forwarded right up to the view
     * @param parent the parent object
     * @param model the resulting model
     * @return the output object. Usually the resulting model or the parent object.
     *         Might be a new object of another type. This is the object that you will
     *         retrieve as a list at the end of the scrapping when using
     *         {@link AutomaticScrapperClient#getResults()} method.
     */
    default Object returnResult(@NotNull final P parent, @NotNull final M model){
        return parent;
    }

    /**
     *
     * This method is called at the end of a scrap to know wether it worked correcly or end up with
     * a bad working scrap
     * @param parent the parent object
     * @param model the resulting model
     * @return a boolean indicating if the model was correctly scrapped. This method is only called
     *         if the scrapper runs up to the end without exceptions. It can be use for statistics
     *         purposes in order to mark it as a failed scrapping because of a missing field for
     *         example altough no exception has been triggered.
     */
    boolean wasScrapped(@NotNull final P parent, @NotNull final M model);


    /**
     * Hook method called when an uncaught exception is triggered during the scrapping process.
     * By default, this method doesn't do anything but can be easily overrided to perform a custom
     * action.
     * @param e The exception thrown
     * @param parent the pareny entity from which we tried to scrap the model M
     * @param model the model output from the current scrap (might be null)
     */
    default void handleException(@NotNull final Throwable e, @NotNull final P parent, final M model){}

}
