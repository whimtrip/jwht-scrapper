package fr.whimtrip.ext.jwhtscrapper.impl;

import fr.whimtrip.core.util.intrf.ExceptionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     Default implementation of {@link ExceptionLogger} that simply
 *     log any exception at warn level.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
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
