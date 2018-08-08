/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.core.util.WhimtripUtils;
import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperAlreadyFinishedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperAlreadyStartedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperUnsupportedException;
import fr.whimtrip.ext.jwhtscrapper.impl.ScrappingStatsImpl;
import fr.whimtrip.ext.jwhtscrapper.intfr.HtmlAutoScrapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.HttpMetrics;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrappingStats;
import fr.whimtrip.ext.jwhtscrapper.service.base.AutomaticInnerScrapperClient;
import fr.whimtrip.ext.jwhtscrapper.service.base.BoundRequestBuilderProcessor;
import fr.whimtrip.ext.jwhtscrapper.service.base.ScrapperThreadCallable;
import fr.whimtrip.ext.jwhtscrapper.service.holder.RequestsScrappingContext;
import fr.whimtrip.ext.jwhtscrapper.service.holder.ScrappingContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 07/08/18</p>
 *
 * <p>
 *     Default and standard implementation of {@link AutomaticInnerScrapperClient}.
 * </p>
 *
 * @param <P> {@inheritDoc}
 * @param <M> {@inheritDoc}
 * @author Louis-wht
 * @since 1.0.0
 */
public final class DefaultAutomaticInnerScrapperClient<P, M> implements AutomaticInnerScrapperClient<P, M> {

    private static final Logger log = LoggerFactory.getLogger(DefaultAutomaticInnerScrapperClient.class);
    private static final long SLEEP_TIME_BETWEEN_GATHERING_OF_RESULTS = 1000;
    private static final int LENGTH_OF_THE_PERCENTAGE_BAR = 100;
    private static final int WIDTH_OF_THE_PERCENTAGE_BAR = 2;
    private static final int LOG_STATUS_EVERY_X_FINISHED_TASKS = 5;

    private final ScrappingContext<P, M, ? extends ScrapperHelper<P, M>> context;

    private final HtmlAutoScrapper<M> htmlAutoScrapper;

    private final ExceptionLogger exceptionLogger;

    private final List<FutureTask<Object>> runningTasks = new ArrayList<>();

    private final List<P> pList = new ArrayList<>();
    private final List results = new ArrayList();

    private final BoundRequestBuilderProcessor requestProcessor;

    private int finishedTasks = 0;
    private int lastFinishedTasksLog = 0;
    private int startedScrapsCount = 0;
    private int validFinishedTasks = 0;
    private int failedFinishedTasks = 0;
    private boolean scrapStarted = false;
    private boolean stopped = false;
    private RequestsScrappingContext requestsScrappingContext;


    /**
     * <p>
     *     Default constructor instance.
     * </p>
     * @param context the scrapping context to use to build and drive the current
     *                scrapping client.
     * @param htmlAutoScrapper the underlying {@link HtmlAutoScrapper} to use to perform
     *                         the scraps.
     * @param exceptionLogger the {@link ExceptionLogger} to use to perform exception
     *                               logging.
     * @param requestProcessor the request processor {@see BoundRequestBuilderProcessor}.
     */
    public DefaultAutomaticInnerScrapperClient(
            ScrappingContext<P, M, ? extends ScrapperHelper<P, M>> context,
            HtmlAutoScrapper<M> htmlAutoScrapper,
            ExceptionLogger exceptionLogger,
            BoundRequestBuilderProcessor requestProcessor
    )
    {
        this.context = context;
        this.htmlAutoScrapper = htmlAutoScrapper;
        this.exceptionLogger = exceptionLogger;
        this.requestProcessor = requestProcessor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List scrap() throws InterruptedException, ExecutionException, ScrapperAlreadyStartedException
    {

        if(scrapStarted)
            throw new ScrapperAlreadyStartedException(this.getClass());

        scrapStarted = true;

        try {
            return innerScrap();
        }
        catch (InterruptedException | ExecutionException e) {
            throw e;
        }
        finally {
            stopped = true;
        }
    }


    /**
     * {@inheritDoc}
     */
    public void addObjectsToScrap(List<P> objectsToScrap){
        synchronized (pList) {
            if(stopped || pList.isEmpty())
                throw new ScrapperAlreadyFinishedException(getContext().getHelper().getClass().getSimpleName());
            pList.addAll(objectsToScrap);
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void terminate() {
        synchronized (runningTasks) {
            stopped = true;
            for (FutureTask ft : runningTasks) {
                ft.cancel(true);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public List<FutureTask<Object>> getRunningTasks() {
        return runningTasks;
    }

    /**
     * {@inheritDoc}
     */
    public ScrappingContext<P, M, ? extends ScrapperHelper<P, M>> getContext() {
        return context;
    }

    /**
     * {@inheritDoc}
     */
    public HttpMetrics getHttpMetrics() throws ScrapperUnsupportedException {
        return htmlAutoScrapper.getHttpMetrics();
    }



    /**
     * {@inheritDoc}
     */
    public ScrappingStats getScrapingStats() {
        if(!scrapStarted)
            return new ScrappingStatsImpl(0,0,0,0, 0);

        int runningTasks = startedScrapsCount - finishedTasks;

        return new ScrappingStatsImpl(
                finishedTasks,
                runningTasks,
                validFinishedTasks,
                failedFinishedTasks,
                pList.size() + runningTasks
        );
    }

    /**
     * <p>
     *     Inner scrapping process. It basically will check for unstarted threads
     *     in a while loop while the queue is not empty and fill those unstarted
     *     threads with new threads ready to be scrapped while creating an
     *     {@link ScrapperThreadCallableImpl} under the hood to properly handle the
     *     each scrapping process.
     * </p>
     * @return a list of objects returned by each different tasks from {@link ScrapperHelper#returnResult(Object, Object)}
     *         method at the end of each scrapping process.
     * @throws ExecutionException if any Execution process was triggered along the way.
     * @throws InterruptedException if the scrapping was interrupted by another thread calling
     *                              {@link #terminate()}.
     */
    @NotNull
    private List innerScrap() throws ExecutionException, InterruptedException {

        requestsScrappingContext = context.getRequestsScrappingContext();

        synchronized (pList) {
            pList.addAll(context.getParentObjects());
        }

        startedScrapsCount = 0;

        Iterator<P> iterator = pList.iterator();
        List<P> pSublist = new ArrayList<>();

        do {
            pSublist.clear();
            pSublist.addAll(newPSublist(iterator));
            iterator = pList.iterator();
            startThreads(pSublist);

            startedScrapsCount += pSublist.size();

            ScrappingResult scrappingResult = waitAndRemoveFinishedThreads();
            if(log.isTraceEnabled())
                log.trace(
                        "failed = {}, valids = {}.",
                        scrappingResult.failed,
                        scrappingResult.valid
                );


            validFinishedTasks += scrappingResult.valid;
            failedFinishedTasks += scrappingResult.valid;

            int delay = context.getRequestsScrappingContext().getRequestsConfig().periodicDelay();

            if(delay > 0 && scrappingResult.valid >= 1) {
                WhimtripUtils.waitForWithOutputToConsole((long)delay, 20);
            }

        } while(startedScrapsCount < requestsScrappingContext.getScrapLimit() && iterator.hasNext() && !stopped);

        while(!runningTasks.isEmpty())
        {
            waitAndRemoveFinishedThreads();
        }

        return results;
    }

    /**
     * <p>
     *     Wait and remove all finished threads while storing stats, logging
     *     status if necessary and storing the results of the scrapping process.
     *     Most of this is handled by {@link #removeFinishedThreads()}.
     * </p>
     * @return the {@link ScrappingResult} for the current threads removal.
     * @throws ExecutionException if an execution exception was triggered
     *                            while calling {@link FutureTask#get()}.
     * @throws InterruptedException if the scrapping was interrupted by another thread calling
     *                              {@link #terminate()}.
     */
    private ScrappingResult waitAndRemoveFinishedThreads()
            throws ExecutionException, InterruptedException {

        try {
            Thread.sleep(SLEEP_TIME_BETWEEN_GATHERING_OF_RESULTS);
        }
        catch(InterruptedException e)
        {
            exceptionLogger.logException(e);
        }
        return removeFinishedThreads();
    }



    /**
     * <p>
     *     Remove all finished threads while storing stats, logging
     *     status if necessary and storing the results of the scrapping process.
     * </p>
     * @return the {@link ScrappingResult} for the current threads removal.
     * @throws ExecutionException if an execution exception was triggered
     *                            while calling {@link FutureTask#get()}.
     * @throws InterruptedException if the scrapping was interrupted by another thread calling
     *                              {@link #terminate()}.
     */
    @SuppressWarnings("unchecked")
    private ScrappingResult removeFinishedThreads()
            throws ExecutionException, InterruptedException
    {
        List<FutureTask> copiedTasks = new ArrayList<>();
        copiedTasks.addAll(runningTasks);
        FutureTask<Object> actFt = null;
        ScrappingResult result = new ScrappingResult();
        for (FutureTask<Object> ft : copiedTasks) {
            actFt = ft;
            try {
                if(ft.isDone()) {

                    if(!((ScrapperFutureTask) ft).hasScrapped())
                    {
                        result.failed++;
                        if(log.isInfoEnabled())
                            log.info(
                                    "Thread n°{} is terminated and wasn't scrapped",
                                    copiedTasks.indexOf(ft)
                            );
                    }

                    else {
                        result.valid ++;
                        results.add(ft.get());
                        if(log.isInfoEnabled())
                            log.info(
                                    "Thread n°{} is terminated and was correctly scrapped",
                                    copiedTasks.indexOf(ft)
                            );
                    }


                    runningTasks.remove(ft);
                    finishedTasks ++;
                }
            }

            catch (InterruptedException | ExecutionException e) {
                finishedTasks ++;
                exceptionLogger.logException(e);

                if(log.isInfoEnabled())
                    log.info("Thread n°{} terminated with an uncaught exception.", copiedTasks.indexOf(ft));

                runningTasks.remove(actFt);
                if (requestsScrappingContext.isThrowExceptions()) {
                    terminate();
                    throw e;
                }
            }

            finally {
                if(lastFinishedTasksLog <= finishedTasks - LOG_STATUS_EVERY_X_FINISHED_TASKS)
                {
                    lastFinishedTasksLog = finishedTasks;
                    logStatusBar();
                }
            }

        }
        return result;
    }


    /**
     * <p>
     *     Simple logging method that will log the current status of the scrap.
     *     If debug logger is enabled then, a sentence describing the current
     *     scrapping percentage will be displayed, if trace is enabled, a status
     *     bar will also be displayed.
     * </p>
     */
    private void logStatusBar() {

        if(!log.isDebugEnabled())
            return;

        int totalScrapCount = getTotalScrapCount();
        int percentageOfTasksFinished = (int)((float) (finishedTasks) / totalScrapCount  * 100 + 0.5);

        log.debug(
                new StringBuilder()
                        .append("There are ")
                        .append(finishedTasks)
                        .append("finished tasks out of ")
                        .append(totalScrapCount)
                        .append(" tasks to run.")
                        .append(percentageOfTasksFinished)
                        .append("% of the scrap was done.")
                        .toString()
        );

        if(!log.isTraceEnabled())
            return;

        int actualNumberOfBars = (int) (((float) percentageOfTasksFinished) / 100 * LENGTH_OF_THE_PERCENTAGE_BAR + 0.5);

        StringBuilder outBuilder = new StringBuilder("\n");

        for (int j = 0; j < WIDTH_OF_THE_PERCENTAGE_BAR; j++) {
            outBuilder.append("||");
            for (int i = 1; i <= LENGTH_OF_THE_PERCENTAGE_BAR; i++) {
                if (i <= actualNumberOfBars)
                    outBuilder.append("#");
                else
                    outBuilder.append("-");
            }
            outBuilder.append("||\n");
        }


        log.trace(outBuilder.toString());
    }

    /**
     * <p>
     *     Simple synchronized method to return the current number of scraps
     *     started plus the current number of scraps remaining. As scraps operations
     *     can be added dynamically using {@link #addObjectsToScrap(List)}, this
     *     number could increase.
     * </p>
     * @return the total number of scraps started plus the size of the tasks to run queue.
     */
    private int getTotalScrapCount() {
        synchronized (pList) {
            return Math.max(
                    requestsScrappingContext.getScrapLimit(),
                    startedScrapsCount + pList.size()
            );
        }
    }


    /**
     * <p>
     *    This method will populate and return a new list with new objects to
     *    start a scrap with. This method will add a new element to the list
     *    until either the queue is empty, the scrap limit was reached or the
     *    number of parrallel running threads is attained. When an element
     *    is added to the new list, it should be removed from the queue. To
     *    ensure thread safety for this piece of code, the processing part
     *    involving queue modification is synchronized on the queue itself
     *    to ensure it is not accessed anywhere else.
     * </p>
     * @param iterator the iterator to extract the next elements from.
     * @return the new lists of elements to start threads from.
     */
    private synchronized List<P> newPSublist(Iterator<P> iterator){
        List<P> pSublist = new ArrayList<>();

        int runningThreads = 0;
        synchronized (runningTasks) {
            runningThreads = runningTasks.size();
        }

        if(log.isDebugEnabled())
            log.debug("There are actually {} running threads.", runningThreads);

        List<P> copiedPList;

        synchronized (pList) {
            copiedPList = new ArrayList<>(pList);

            while (
                    startedScrapsCount < requestsScrappingContext.getScrapLimit()
                 && runningThreads < requestsScrappingContext.getParrallelThreads()
                 && iterator.hasNext()
            ){
                P p = iterator.next();
                pSublist.add(p);
                copiedPList.remove(p);
                startedScrapsCount++;
                runningThreads++;
            }

            pList.clear();
            pList.addAll(copiedPList);

        }

        return pSublist;
    }


    /**
     *
     * @param pSublist the threads to store and start.
     * @return the instanciated FutureTasks that will run the new tasks.
     */
    @SuppressWarnings("unchecked")
    private List<FutureTask<Object>> startThreads(List<P> pSublist)
    {
        List<FutureTask<Object>> fts = new ArrayList<>();

        int numberOfThreadsStarted = 0;
        for(P p : pSublist)
        {

            if(stopped)
                break;

            FutureTask<Object> ft = new ScrapperFutureTask<>(
                    new ScrapperThreadCallableImpl(p, context, htmlAutoScrapper, requestProcessor)
            );
            fts.add(ft);
            runningTasks.add(ft);

            if(log.isInfoEnabled())
                log.info(
                        "Starting thread n°{}",
                        (startedScrapsCount + numberOfThreadsStarted)
                );

            numberOfThreadsStarted++;
        }

        for(FutureTask<Object> ft : fts)
        {
            Thread t = new Thread(ft);
            t.start();
        }

        return fts;
    }


    /**
     * Small holder class that will return both valid and failed counts
     * when gathering results through {@link #removeFinishedThreads()}.
     */
    private static class ScrappingResult {
        int valid = 0;
        int failed = 0;
    }

    /**
     * <p>
     *     Small Future Tasks extending class that features two additional methods
     *     to handle properly the two additionnal methods of {@link ScrapperThreadCallable}.
     * </p>
     * @param <P> the Parent class type.
     * @param <M> the mapped model class type.
     */
    private static class ScrapperFutureTask<P, M> extends FutureTask<Object>
    {
        private ScrapperThreadCallable<P,M> callable;

        private ScrapperFutureTask(@NotNull ScrapperThreadCallable<P, M> callable) {
            super(callable);
            this.callable = callable;
        }

        private boolean hasScrapped(){
            return callable.hasScrapped();
        }

        @Override
        public boolean isDone()
        {
            return callable.isDone();
        }
    }
}
