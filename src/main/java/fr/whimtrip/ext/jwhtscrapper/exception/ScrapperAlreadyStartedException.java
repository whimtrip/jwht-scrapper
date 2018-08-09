package fr.whimtrip.ext.jwhtscrapper.exception;

import fr.whimtrip.ext.jwhtscrapper.intfr.AutomaticScrapperClient;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 27/07/18</p>
 *
 * <p>
 *     Exception thrown in {@link AutomaticScrapperClient#scrap()} when
 *     scrapping is run twice.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class ScrapperAlreadyStartedException extends ScrapperException {


    public ScrapperAlreadyStartedException(Class scrapperClazz) {
        super("Scrapping is already running for scrapper of class " + scrapperClazz);
    }
}
