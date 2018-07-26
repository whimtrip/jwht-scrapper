/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.intfr;

import fr.whimtrip.ext.jwhtscrapper.service.scoped.LinkPreparatorHolder;
import org.asynchttpclient.BoundRequestBuilder;

import java.lang.reflect.Field;

/**
 * Created by LOUISSTEIMBERG on 19/11/2017.
 */
public interface HttpRequestEditor<T,U> {

    /**
     * Gather field parameters in order to prepare the request Editor
     * @param field the field to analyze
     */
    void init(Field field);

    /**
     * Called with the parent object that will contain the children one
     * in order to know wether this link should be visited or not
     * @param parentContainer the parent object
     * @return a boolean indicating wether the request should be performed
     *         or not.
     */
    boolean doRequest(T parentContainer);

    /**
     * Called with the newly instanciated object in order to prepare this object
     * @param obj the return object to prepare.
     * @param parentContainer the parent object
     * @param linkPreparatorHolder the link preparator holder that holds all necessary
     *                             informations to perform the request correctly.
     */
    void prepareObject(U obj, T parentContainer, LinkPreparatorHolder linkPreparatorHolder);

    /**
     *  Called to edit the request, to add fields, or headers, etc...
     * @param req the request to edit.
     * @param preparatorHolder the link preparator holder holding all needed request
     *                         information
     * @return the edited request. Might not be the same object reference.
     */
    BoundRequestBuilder editRequest(BoundRequestBuilder req, LinkPreparatorHolder preparatorHolder);
}
