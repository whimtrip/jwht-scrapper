package fr.whimtrip.ext.jwhtscrapper.exception;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class ScrapperUnsupportedException extends ScrapperException {

    public ScrapperUnsupportedException(String methodName, Class nonSupportingOperationClass) {
        super(String.format("%s method is not supported by %s.", methodName, nonSupportingOperationClass));
    }
}
