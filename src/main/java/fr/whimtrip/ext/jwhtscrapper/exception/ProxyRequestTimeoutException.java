package fr.whimtrip.ext.jwhtscrapper.exception;

public class ProxyRequestTimeoutException extends ScrapperException {

    public ProxyRequestTimeoutException(Throwable cause)
    {
        super(cause.getMessage());
        setStackTrace(cause.getStackTrace());
    }
}
