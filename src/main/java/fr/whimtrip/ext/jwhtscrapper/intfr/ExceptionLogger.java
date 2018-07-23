package fr.whimtrip.ext.jwhtscrapper.intfr;

public interface ExceptionLogger {

    void logException(Throwable exception);

    void logException(Throwable exception, boolean b);
}
