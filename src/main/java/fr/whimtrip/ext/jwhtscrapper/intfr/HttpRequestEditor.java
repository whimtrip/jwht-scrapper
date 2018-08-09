/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.intfr;

import fr.whimtrip.ext.jwhtscrapper.annotation.Link;
import fr.whimtrip.ext.jwhtscrapper.service.base.BoundRequestBuilderProcessor;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkPreparatorHolder;
import org.asynchttpclient.BoundRequestBuilder;

import java.lang.reflect.Field;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 30/07/18</p>
 *
 * <p>
 *     This interface can be used with {@link Link} annotation
 *     and will help defines hooks to intercept and customize the
 *     request before it will be sent.
 * </p>
 *
 * @param <P> the parent POJO type
 * @param <U> the resulting child POJO from the scrap to come.
 * @author Louis-wht
 * @since 1.0.0
 */
public interface HttpRequestEditor<P,U> {

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
    boolean shouldDoRequest(P parentContainer);

    /**
     * Called with the newly instanciated object in order to prepare this object
     * @param obj the return object to prepare.
     * @param parentContainer the parent object
     * @param linkPreparatorHolder the link preparator holder that holds all necessary
     *                             informations to perform the request correctly.
     */
    void prepareObject(U obj, P parentContainer, LinkPreparatorHolder<P> linkPreparatorHolder);

    /**
     *  Called to edit the request, to add fields, or headers, etc...
     * @param req the request to edit.
     * @param preparatorHolder the link preparator holder holding all needed request
     *                         information
     * @param  requestProcessor the request processor unit that will be used to manipulate
     *                          the request.
     */
    void editRequest(BoundRequestBuilder req, LinkPreparatorHolder<P> preparatorHolder, BoundRequestBuilderProcessor requestProcessor);
}
