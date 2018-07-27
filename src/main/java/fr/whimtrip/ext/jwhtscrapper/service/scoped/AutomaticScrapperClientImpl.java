package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapFailedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapNotFinishedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperAlreadyStartedException;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrappingStats;
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
public class AutomaticScrapperClientImpl<P, M> implements AutomaticScrapperClient<P, M> {

    private final AutomaticInnerScrapperClient scrapperClient;
    private final ExceptionLogger exceptionLogger;

    private boolean scrapped = false;
    private boolean scrapStarted = false;

    private FutureTask<List<M>> ft;

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
     * @see AutomaticScrapperClient#scrap()
     * @throws ScrapperAlreadyStartedException see {@link AutomaticScrapperClient#scrap()}
     */
    public synchronized void scrap() throws ScrapperAlreadyStartedException {

        if (!scrapped && !scrapStarted) {

            scrapStarted = true;
            ft = new FutureTask<List<M>>(() -> {

                List<M> results = null;
                try {
                    results = scrapperClient.scrap();
                } catch (ExecutionException | InterruptedException e) {
                    scrapperClient.stopRunningTasks();
                    exceptionLogger.logException(e.getCause());
                    throw new ScrapFailedException(e.getCause());
                }

                scrapped = true;

                return results;
            });

            new Thread(ft, getScrapperThreadName()).start();
        }
        else throw new ScrapperAlreadyStartedException(this.getClass());
    }


    /**
     * @see AutomaticScrapperClient#addObjectsToScrap(List)
     * @param l see {@link AutomaticScrapperClient#addObjectsToScrap(List)}
     */
    public synchronized void addObjectsToScrap(List<P> l) {
        if(!scrapped && scrapStarted)
            scrapperClient.addPElements(l);
    }

    /**
     * @see AutomaticScrapperClient#getScrappingStats()
     * @return see {@link AutomaticScrapperClient#getScrappingStats()}
     */
    public ScrappingStats getScrappingStats(){
        return scrapperClient.getScrapingStats();
    }

    /**
     * @see AutomaticScrapperClient#isScrapped()
     * @return see {@link AutomaticScrapperClient#isScrapped()}
     */
    public boolean isScrapped() {
        return scrapped;
    }

    /**
     * @see AutomaticScrapperClient#getResults()
     * @return see {@link AutomaticScrapperClient#getResults()}
     * @throws ScrapFailedException see {@link AutomaticScrapperClient#getResults()}
     * @throws ScrapNotFinishedException see {@link AutomaticScrapperClient#getResults()}
     */
    public List<M> getResults() throws ScrapFailedException, ScrapNotFinishedException {

        return getResults(null, null);
    }

    /**
     * @see AutomaticScrapperClient#getResults(Long, TimeUnit)
     * @param timeout see {@link AutomaticScrapperClient#getResults(Long, TimeUnit)}
     * @param timeUnit the {@link TimeUnit} of the {@code timeout}.
     * @return  see {@link AutomaticScrapperClient#getResults(Long, TimeUnit)}
     * @throws ScrapFailedException  see {@link AutomaticScrapperClient#getResults(Long, TimeUnit)}
     * @throws ScrapNotFinishedException  see {@link AutomaticScrapperClient#getResults(Long, TimeUnit)}
     */
    public List<M> getResults(Long timeout, TimeUnit timeUnit) throws ScrapFailedException, ScrapNotFinishedException {

        if (!scrapped && timeout == null) throw new ScrapNotFinishedException(getScrapperThreadName());

        try {
            return timeout == null ? ft.get() : ft.get(timeout, timeUnit);
        }

        catch (InterruptedException | ExecutionException e) {
            throw new ScrapFailedException(e instanceof ExecutionException ? e.getCause() : e);
        }

        catch (TimeoutException e) {
            throw new ScrapNotFinishedException(e, getScrapperThreadName());
        }
    }

    /**
     * @see AutomaticScrapperClient#terminate()
     */
    public void terminate() {
        scrapperClient.stopRunningTasks();
    }

    /**
     *
     * @return the scrapper thread name.
     */
    @NotNull
    private String getScrapperThreadName() {

        return "Scrapper-" + scrapperClient.getContext().getHelper().getClass().getSimpleName();
    }
}
