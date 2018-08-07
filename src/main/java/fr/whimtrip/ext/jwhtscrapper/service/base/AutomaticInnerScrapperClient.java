package fr.whimtrip.ext.jwhtscrapper.service.base;

import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperAlreadyFinishedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperAlreadyStartedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperUnsupportedException;
import fr.whimtrip.ext.jwhtscrapper.intfr.HttpMetrics;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrappingStats;
import fr.whimtrip.ext.jwhtscrapper.service.holder.RequestsScrappingContext;
import fr.whimtrip.ext.jwhtscrapper.service.holder.ScrappingContext;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 07/08/18</p>
 *
 * <p>
 *     Inner Client class that should perform parralelizing of scraps to be
 *     started in multiple threads while filling threads with new scraps when
 *     terminated so that the accounted max number of parrallel threads is
 *     always reached until the queue of scraps unstarted is not empty.
 * </p>
 *
 * <p>
 *     It should also be able to accept new elements to scrap in a thread
 *     safe manner.
 * </p>
 *
 * <p>
 *     The implementing class should also be able to stop all current running tasks
 *     and terminate the scrapping process.
 * </p>
 *
 * <p>
 *     Finally, returning running tasks, scrapping context, current {@link HttpMetrics}
 *     and {@link ScrappingStats} should also be part of the implementing class processing
 *     unit.
 * </p>
 *
 * @param <P> Parent Type
 * @param <M> Model on which response body will be mapped
 * @author Louis-wht
 * @since 1.0.0
 */
public interface AutomaticInnerScrapperClient<P, M> {


    /**
     * <p>
     *     Main method of this processing unit. It should be thread safe
     *     to ensure {@link ScrapperAlreadyStartedException} is thrown
     *     correctly and no two scraps are started from two separate
     *     threads on the same client, leading to further potential leaks
     *     and other uncontrolled exceptions.
     * </p>
     * <p>
     *     This method should empty the queue of tasks to be run while not
     *     starting more than the maximum number of threads allowed by the
     *     inner {@link ScrappingContext}. Therefore it should replace
     *     periodically finished threads with new scraps to run.
     * </p>
     * <p>
     *     This method might be synchronous running the whole scrap before
     *     returning its result. It should basically be wrapped into a
     *     {@link AutomaticScrapperClient} that will handle asynchronous
     *     thread start, terminations messages and new scraps queuing.
     * </p>
     * @return the results of the scrapping process which is the list of objects
     *         returned by each scrap from the method {@link ScrapperHelper#returnResult(Object, Object)}.
     *
     * @throws InterruptedException if the one of the scrapping gets interrupted.
     *                              Will typically happen when {@link #terminate()}
     *                              is called.
     * @throws ExecutionException when one of the scrapping failed with an uncaught exception
     *                            if request context {@link RequestsScrappingContext#isThrowExceptions()}
     *                            returns true.
     * @throws ScrapperAlreadyStartedException if called twice. Each single client
     *                                         should be able to start a scrap only
     *                                         once in its lifetime.
     */
    List scrap() throws InterruptedException, ExecutionException, ScrapperAlreadyStartedException;


    /**
     * <p>
     *     This method will add new elements to the queue of scraps to perform
     *     in separate new threads and should be synchronized.
     * </p>
     * @param objectsToScrap the list of new elements to scrap.
     * @throws ScrapperAlreadyFinishedException when the scrap is already ternminated
     *                                          or the queue of scraps to run has been
     *                                          emptied.
     */
    void addObjectsToScrap(List<P> objectsToScrap) throws ScrapperAlreadyFinishedException;


    /**
     * <p>
     *     This method should interrupt the running tasks and stop the current scrapping
     *     process so that no other remaining task can be run afterward. This is definitive
     *     and can't be rolled back. This method should warranty thread safe access.
     * </p>
     *
     */
    void terminate();


    /**
     * @return the current running tasks.
     */
    List<FutureTask<Object>> getRunningTasks();


    /**
     * @return the {@link ScrappingContext} used to build and drive this
     *         {@link AutomaticInnerScrapperClient}
     */
    ScrappingContext<P, M, ? extends ScrapperHelper<P, M>> getContext();


    /**
     * @see HttpMetrics
     * @return the current {@link HttpMetrics}.
     * @throws ScrapperUnsupportedException if the underlying {@link HttpManagerClient}
     *                                      or other processing unit used does not support
     *                                      returning valid Http Metrics.
     */
    HttpMetrics getHttpMetrics() throws ScrapperUnsupportedException;


    /**
     * @see ScrappingStats
     * @return the current {@link ScrappingStats}.
     */
    ScrappingStats getScrapingStats();



}
