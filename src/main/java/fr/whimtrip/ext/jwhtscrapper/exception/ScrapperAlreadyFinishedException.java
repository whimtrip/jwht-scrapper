package fr.whimtrip.ext.jwhtscrapper.exception;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 07/08/18</p>
 *
 * <p>
 *     Thrown when scrapper client has already finish processing
 *     its tasks and a runtime-only method is being used on the
 *     scrapper client instance.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class ScrapperAlreadyFinishedException extends ScrapperException {

    public ScrapperAlreadyFinishedException(@NotNull final String scrapperName) {
        super(String.format("Scrapper %s is already finished, no other operations can be performed on it!", scrapperName));
    }
}
