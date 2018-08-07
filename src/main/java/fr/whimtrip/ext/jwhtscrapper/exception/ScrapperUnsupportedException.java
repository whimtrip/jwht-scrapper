package fr.whimtrip.ext.jwhtscrapper.exception;

import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     If a class implemented from a jwht-scrapper interface does
 *     not support a feature. Yet it will only be accepted on
 *     {@link HttpManagerClient#getHttpMetrics()} and other subsequent
 *     methods using this method under the hood.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class ScrapperUnsupportedException extends ScrapperException {

    public ScrapperUnsupportedException(String methodName, Class nonSupportingOperationClass) {
        super(String.format("%s method is not supported by %s.", methodName, nonSupportingOperationClass));
    }
}
