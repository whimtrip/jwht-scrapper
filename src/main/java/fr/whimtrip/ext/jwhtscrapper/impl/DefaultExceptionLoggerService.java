package fr.whimtrip.ext.jwhtscrapper.impl;

import fr.whimtrip.ext.jwhtscrapper.intfr.ExceptionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultExceptionLoggerService implements ExceptionLogger {

    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionLoggerService.class);

    @Override
    public void logException(Throwable exception) {
        log.warn("Exception Triggered ", exception);
    }

    @Override
    public void logException(Throwable exception, boolean printStacktrace) {
        if(printStacktrace)
            logException(exception);
    }
}
