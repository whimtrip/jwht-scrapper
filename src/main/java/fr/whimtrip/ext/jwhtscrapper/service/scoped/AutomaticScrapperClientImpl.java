package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapFailedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapNotFinishedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperException;
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
 * @author Louis-wht
 * @since 26/07/18
 */
public class AutomaticScrapperClientImpl<P, M> implements AutomaticScrapperClient<P, M> {

    private final AutomaticInnerScrapperClient scrapperClient;
    private final ExceptionLogger exceptionLogger;

    private boolean scrapped = false;
    private boolean scrapStarted = false;

    private FutureTask<List<M>> ft;

    public AutomaticScrapperClientImpl(AutomaticInnerScrapperClient scrapperClient, ExceptionLogger exceptionLogger) {

        this.scrapperClient = scrapperClient;
        this.exceptionLogger = exceptionLogger;
    }

    public synchronized void scrap() throws ScrapperException {

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
        else throw new ScrapperException("AutomaticScrapperClient instance cannot be scrapped twice");
    }


    public synchronized void addObjectsToScrap(List<P> l) {
        if(!scrapped && scrapStarted)
            scrapperClient.addPElements(l);
    }

    public ScrappingStats getScrappingStats(){
        return scrapperClient.getScrapingStats();
    }

    public boolean isScrapped() {
        return scrapped;
    }

    public List<M> getResults() throws ScrapperException {
        return getResults(null, null);
    }

    public List<M> getResults(Long timeout, TimeUnit timeUnit) throws ScrapperException {

        if (!scrapped && timeout == null) throw new ScrapNotFinishedException(getScrapperThreadName());

        try {
            return getValue(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ScrapFailedException(e.getCause());
        }
    }

    public void terminate() {
        scrapperClient.stopRunningTasks();
    }

    private List<M> getValue(Long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return timeout == null ? ft.get() : ft.get(timeout, timeUnit);
    }

    @NotNull
    private String getScrapperThreadName() {

        return "Scrapper-" + scrapperClient.getContext().getHelper().getClass().getSimpleName();
    }
}
