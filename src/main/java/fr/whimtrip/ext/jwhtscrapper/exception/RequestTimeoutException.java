package fr.whimtrip.ext.jwhtscrapper.exception;

public class RequestTimeoutException extends ScrapperException {

    public RequestTimeoutException(Throwable cause)
    {
        super(cause.getMessage());
        setStackTrace(cause.getStackTrace());
    }
}
