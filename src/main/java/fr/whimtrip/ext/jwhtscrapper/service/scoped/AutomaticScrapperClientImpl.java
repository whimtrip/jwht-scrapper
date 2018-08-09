package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.exception.*;
import fr.whimtrip.ext.jwhtscrapper.intfr.HttpMetrics;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrappingStats;
import fr.whimtrip.ext.jwhtscrapper.service.base.AutomaticInnerScrapperClient;
import fr.whimtrip.ext.jwhtscrapper.service.base.AutomaticScrapperClient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>Part of project jwht-scrapper</p>
 *
 * <p>Default implementation of {@link AutomaticScrapperClient}</p>
 * @see AutomaticScrapperClient
 * @author Louis-wht
 * @since 26/07/18
 */
public final class AutomaticScrapperClientImpl<P> implements AutomaticScrapperClient<P> {

    private final AutomaticInnerScrapperClient scrapperClient;
    private final ExceptionLogger exceptionLogger;

    private boolean scrapped = false;
    private boolean scrapStarted = false;

    private FutureTask<List> ft;

    /**
     * Default Constructor.
     * @param scrapperClient the inner scrapper client implementation.
     * @param exceptionLogger the exception logger to use when catching an error
     *                        to be logged.
     */
    public AutomaticScrapperClientImpl(AutomaticInnerScrapperClient scrapperClient, ExceptionLogger exceptionLogger) {

        this.scrapperClient = scrapperClient;
        this.exceptionLogger = exceptionLogger;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void scrap() throws ScrapperAlreadyStartedException {

        if (!scrapped && !scrapStarted) {

            scrapStarted = true;
            ft = new FutureTask<List>(() -> {

                List results = null;

                try {
                    results = scrapperClient.scrap();
                }

                catch (ExecutionException | InterruptedException e) {
                    scrapperClient.terminate();
                    exceptionLogger.logException(e.getCause());
                    throw new ScrapFailedException(e.getCause());
                }

                finally {
                    scrapped = true;
                }


                return results;
            });

            new Thread(ft, getScrapperThreadName()).start();
        }
        else throw new ScrapperAlreadyStartedException(this.getClass());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void add(List<P> l)  throws ScrapperAlreadyFinishedException {
        if(!scrapped && scrapStarted)
            scrapperClient.addObjectsToScrap(l);
        else
            throw new ScrapperAlreadyFinishedException(getScrapperThreadName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScrappingStats getScrappingStats(){
        return scrapperClient.getScrapingStats();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public HttpMetrics getHttpMetrics() throws ScrapperUnsupportedException {
        return scrapperClient.getHttpMetrics();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isScrapped() {
        return scrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List getResults() throws ScrapFailedException, ScrapNotFinishedException {

        return getResults(null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List waitAndGetResults() throws ScrapFailedException {
        return getResults(null, null, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List getResults(Long timeout, TimeUnit timeUnit) throws ScrapFailedException, ScrapNotFinishedException {

        return getResults(timeout, timeUnit, true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void terminate() {
        scrapperClient.terminate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompleted() {
        return scrapped;
    }

    /**
     *
     * @return the scrapper thread name.
     */
    @NotNull
    private String getScrapperThreadName() {

        return "Scrapper-" + scrapperClient.getContext().getHelper().getClass().getSimpleName();
    }


    /**
     * @param timeout see {@link #getResults(Long, TimeUnit)}
     * @param timeUnit see {@link #getResults(Long, TimeUnit)}
     * @param checkFinishedScrap wether an task completion should be checked before getting
     *                           the result.
     * @throws ScrapFailedException see {@link #getResults(Long, TimeUnit)}
     * @throws ScrapNotFinishedException see {@link #getResults(Long, TimeUnit)}
     * @return see {@link #getResults(Long, TimeUnit)}
     */
    private List getResults(Long timeout, TimeUnit timeUnit, boolean checkFinishedScrap) throws ScrapFailedException, ScrapNotFinishedException  {

        if (checkFinishedScrap && !scrapped && timeout == null) throw new ScrapNotFinishedException(getScrapperThreadName());

        try {
            return timeout == null ? ft.get() : ft.get(timeout, timeUnit);
        }

        catch (InterruptedException | ExecutionException e) {
            throw new ScrapFailedException(e instanceof ExecutionException ? e.getCause() : e);
        }

        catch (TimeoutException e) {
            terminate();
            throw new ScrapNotFinishedException(e, getScrapperThreadName());
        }
    }
}
