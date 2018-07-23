package fr.whimtrip.ext.jwhtscrapper.exception;

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
