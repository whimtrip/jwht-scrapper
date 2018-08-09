/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.exception;

import fr.whimtrip.ext.jwhtscrapper.intfr.AutomaticScrapperClient;
import fr.whimtrip.ext.jwhtscrapper.service.base.ScrapperThreadCallable;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     Thrown when the scrapping process has failed. This exception
 *     happens when an uncaught exception appears in the processing
 *     scope of {@link ScrapperThreadCallable} and is further catched
 *     and thrown as this current exception class within {@link AutomaticScrapperClient}
 *     scope.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class ScrapFailedException extends ScrapperException {

    public ScrapFailedException(Throwable e)
    {
        super("Scrap failed with exception " + e.getMessage());
        super.setStackTrace(e.getStackTrace());
    }
}
