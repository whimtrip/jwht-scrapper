package fr.whimtrip.ext.jwhtscrapper.exception;


/**
 *
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     Default Scrapper Exception all exception classes of this project
 *     will extend. It can also be used as a standalone altough creating
 *     your own extending exception class is recommended if necessary.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class ScrapperException extends RuntimeException {

    public ScrapperException() {

        super();
    }

    public ScrapperException(String message) {

        super(message);
    }

    public ScrapperException(String message, Throwable cause) {

        super(message, cause);
    }

    public ScrapperException(Throwable cause) {

        super(cause);
    }

    protected ScrapperException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {

        super(message, cause, enableSuppression, writableStackTrace);
    }
}
