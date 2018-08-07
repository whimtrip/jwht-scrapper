package fr.whimtrip.ext.jwhtscrapper.exception;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     Thrown when a request times out. It should be caught very
 *     early.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class RequestTimeoutException extends ScrapperException {

    public RequestTimeoutException(Throwable cause)
    {
        super(cause.getMessage());
        setStackTrace(cause.getStackTrace());
    }
}
