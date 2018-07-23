/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.exception;

/**
 * Created by LOUISSTEIMBERG on 22/11/2017.
 */
public class ScrapFailedException extends ScrapperException {

    public ScrapFailedException(Throwable e)
    {
        super("Scrap failed with exception " + e.getMessage());
        super.setStackTrace(e.getStackTrace());
    }
}
