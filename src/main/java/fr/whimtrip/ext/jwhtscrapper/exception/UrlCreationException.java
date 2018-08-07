package fr.whimtrip.ext.jwhtscrapper.exception;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 07/08/18</p>
 *
 * <p>
 *     Triggered when a scrapping url cannot be properly created.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class UrlCreationException extends ScrapperException {
    public UrlCreationException(String message) {
        super("Url could not be properly created, detailed explanation is : " + message);
    }
}
