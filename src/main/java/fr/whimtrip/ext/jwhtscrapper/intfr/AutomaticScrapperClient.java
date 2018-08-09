package fr.whimtrip.ext.jwhtscrapper.intfr;

import fr.whimtrip.ext.jwhtscrapper.exception.ScrapFailedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapNotFinishedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperAlreadyFinishedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperAlreadyStartedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperUnsupportedException;
import fr.whimtrip.ext.jwhtscrapper.impl.ScrappingStatsImpl;
import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     This interface defines how a custom {@link AutomaticScrapperClient}
 *     should behave in case you'd like to provide your custom implementation
 *     and plug it somewhere.
 * </p>
 *
 * <p>Any implementing class should be able to :</p>
 * <ul>
 *     <li>Start the scrapping operation asynchronously in a separate thread.</li>
 *     <li>
 *         Add new objects to scrap during the operation if the scrap is not
 *         terminated.
 *     </li>
 *     <li>
 *         Return {@link ScrappingStats} to describe the current state of the
 *         scrapping
 *     </li>
 *     <li>Tell if the scrap is finished or not.</li>
 *     <li>
 *         Return the results of the scrapping (which is a list of the objects
 *         returned by {@link ScrapperHelper#returnResult(Object, Object)}).
 *     </li>
 *     <li>Terminate the scrapper while closing all current running tasks.</li>
 * </ul>
 * @param <P> the parent type whose instances will help initiating each scrap.
 * @author Louis-wht
 * @since 1.0.0
 */
public interface AutomaticScrapperClient<P> {


    /**
     * <p>
     *     Should start the scrapping in a dedicated Thread.
     *     Overriding methods should be {@code synchronized}
     *     to avoid several scraping being run concurrently.
     * </p>
     * @throws ScrapperAlreadyStartedException if another scrap is already
     *                                         running with the same instance
     */
    void scrap() throws ScrapperAlreadyStartedException;


    /**
     * @param l the list of {@code <P>} objects to add to the queue of objects
     *          to be scrapped.
     *          This method must be synchronized and / or provide a way to add
     *          elements in a synchronous way to avoid concurrency problems
     *          occurring when performing multiple read and write operations on
     *          the subjacent master list.
     * @throws ScrapperAlreadyFinishedException when the scrap is already ternminated
     *                                          or the queue of scraps to run has been
     *                                          emptied.
     */
    void add(List<P> l) throws ScrapperAlreadyFinishedException;


    /**
     * @return <p>
     *             Return the results of the scrapping (which is a list of the objects
     *             returned by {@link ScrapperHelper#returnResult(Object, Object)}).
     *         </p>
     *
     * @throws ScrapFailedException If the underlying {@link Callable#call()} throws
     *                              an exception, or if the thread execution was
     *                              interrupted.
     *
     * @throws ScrapNotFinishedException If the method was called when {@link #isCompleted()}
     *                                   still returns {@code false}.
     */
    List getResults() throws ScrapFailedException, ScrapNotFinishedException;



    /**
     * @return <p>
     *             Return the results of the scrapping (which is a list of the objects
     *             returned by {@link ScrapperHelper#returnResult(Object, Object)}).
     *         </p>
     *         <p>
     *             This method will wait synchronously for the scrap to end.
     *         </p>
     *
     *
     * @throws ScrapFailedException If the underlying {@link Callable#call()} throws
     *                              an exception, or if the thread execution was
     *                              interrupted.
     *
     */
    List waitAndGetResults() throws ScrapFailedException;

    /**
     * @param timeout <p>
     *                  the time that this method will be waiting for the underlying
     *                  scrapping thread to end.
     *                </p>
     *                <p>
     *                  If a null value is provided then,
     *                  it's equivalent to the the timeout being equal to 0. Therefore
     *                  {@link ScrapNotFinishedException} will be thrown if {@link #isCompleted()}
     *                  returns {@code false}.
     *                </p>
     * @param timeUnit the {@link TimeUnit} of the {@code timeout}.
     * @return <p>
     *             Return the results of the scrapping (which is a list of the objects
     *             returned by {@link ScrapperHelper#returnResult(Object, Object)}).
     *         </p>
     *
     * @throws ScrapFailedException
     *        <p>
     *            If the underlying {@link Callable#call()} throws an exception, or if
     *            the thread execution was interrupted.
     *        </p>
     *        <p>
     *            This method will trigger under the hood {@link FutureTask#get()}
     *            method which means that it will wait for the underlying thread
     *            to end for the submitted {@code timeout}.
     *        </p>
     * @throws ScrapNotFinishedException
     *         <p>if once the timeout is reached, the scrapper still runs.</p>
     *         <p>
     *             <strong>
     *                 Warning! Once this exception has been thrown once,
     *                 the scrapper won't run anymore and will call under
     *                 the hood {@link #terminate()}.
     *                 To continue scrapping, you should instanciate a new
     *                 {@link AutomaticScrapperClient}.
     *             </strong>
     *         </p>
     */
    List getResults(Long timeout, TimeUnit timeUnit) throws ScrapFailedException, ScrapNotFinishedException;

    /**
     * This method should close all running thread and keep the Scrapper
     * from opening new threads.
     *             <strong>
     *                 Warning! Once this method has been called once, this
     *                 scrapper cannot be reused.
     *                 To continue scrapping, you should instanciate a new
     *                 {@link AutomaticScrapperClient}.
     *             </strong>
     */
    void terminate();

    /**
     * @return {@code true} if the task is either completed or terminated,
     *         {@code false} otherwise.
     */
    boolean isCompleted();

    /**
     * @return <p>
     *              current stats of the {@link AutomaticScrapperClient}.
     *              Default implementation will return an {@link ScrappingStatsImpl}
     *              which is an immutable implementation of {@link ScrappingStats}.
     *              Therefore, it won't be updated when the scrapping stats
     *              will be modified. Rather than this, you should reuse this
     *              method once again.
     *         </p>
     *         <p>
     *             If your implementation features mutable implementation of
     *             {@link ScrappingStats} and if this method returns always the
     *             same object reference, you can implement this method much in
     *             a getter style way.
     *         </p>
     */
    ScrappingStats getScrappingStats();

    /**
     * @return the current Http metrics {@link HttpMetrics} of the current instance of
     *         the HttpManagerClient.
     * @throws ScrapperUnsupportedException If your implementation does not support returning
     *                                      or if underlying classes does not support returning
     *                                      HttpMetrics. (see {@link HttpManagerClient} and
     *                                      {@link HtmlAutoScrapper}).
     */
    @NotNull
    HttpMetrics getHttpMetrics() throws ScrapperUnsupportedException;

}
