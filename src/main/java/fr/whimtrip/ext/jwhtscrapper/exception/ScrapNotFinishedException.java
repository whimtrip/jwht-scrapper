package fr.whimtrip.ext.jwhtscrapper.exception;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     Thrown when the scrapping process of a Scrapping client
 *     is not finished and a scrap-finished-only method is called
 *     on this same client.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class ScrapNotFinishedException extends ScrapperException {
    public ScrapNotFinishedException(String scrapperThreadName) {
        super(
                "Scrapper " + scrapperThreadName + " has not yet finished running. To abort its" +
                " process manually, please use AutomaticScrapperClient.terminate() method or use" +
                " a timeout in the AutomaticScrapperClient.getResults(Long Timeout, TimeUnit timeUnit)" +
                " method"
        );
    }

    public ScrapNotFinishedException(TimeoutException e, String scrapperThreadName) {
        super(
                "Scrapper " + scrapperThreadName + " has not yet finished running. The" +
                " process was aborted by the " + ExecutionException.class + " triggered" +
                " because of the timeout provided."
        );
        initCause(e);

    }
}
