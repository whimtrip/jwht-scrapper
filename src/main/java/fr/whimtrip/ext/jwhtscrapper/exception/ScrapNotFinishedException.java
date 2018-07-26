package fr.whimtrip.ext.jwhtscrapper.exception;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
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
}
