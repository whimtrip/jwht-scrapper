package fr.whimtrip.ext.jwhtscrapper.service.base;

import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.HtmlAutoScrapper;

import java.util.concurrent.Callable;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 27/07/18</p>
 *
 * <p>
 *     The {@link ScrapperThreadCallable} specific implementations that will
 *     trigger each single scrap operation from each unique item of the input
 *     parent objects list. This will use both the {@link ScrapperHelper}
 *     implementation provided by the end-user, and the {@link HtmlAutoScrapper}
 *     instance provided through the builders used to create the current
 *     {@link AutomaticScrapperClient}.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 * @param <P> P is the parent object which will be used each time to create the request and that
 *           in the end will be modified.
 * @param <M> M is the model on which the Html responses will be mapped
 */
public interface ScrapperThreadCallable<P, M> extends Callable<Object> {


    /**
     * @return a boolean indicating wether the task is finished and ready
     *         to be gathered or not. Finished tasks does not imply the
     *         scrap was sucessful. It might have ended with an exception
     *         or {@link ScrapperHelper#wasScrapped(Object, Object)}
     *         method might have returned {@code false}.
     */
    boolean isDone();

    /**
     * @return a boolean indicating wether the scrap was successful or not.
     *         In order for this method to return {@code true}, {@link #isDone()}
     *         must return {@code true} and {@link ScrapperHelper#wasScrapped(Object, Object)}
     *         must return {@code true} as well. No exception must have been
     *         triggered within the callable scope. Otherwise, {@code false}
     *         will be returned.
     */
    boolean hasScrapped();
}
