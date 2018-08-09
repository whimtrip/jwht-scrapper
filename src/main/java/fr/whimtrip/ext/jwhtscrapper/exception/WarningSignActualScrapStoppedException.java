package fr.whimtrip.ext.jwhtscrapper.exception;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 09/08/18</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class WarningSignActualScrapStoppedException extends  WarningSignException {

    public WarningSignActualScrapStoppedException(WarningSignException e) {

        super(e.getField());
        pausingBehavior = e.getPausingBehavior();
        action = e.getAction();
    }
}
