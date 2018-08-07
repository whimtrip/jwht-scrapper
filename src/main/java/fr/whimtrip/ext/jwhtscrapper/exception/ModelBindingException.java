/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.exception;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     Thrown when any raw HTTP body to POJO mapping exception
 *     occures.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class ModelBindingException extends ScrapFailedException {
    public ModelBindingException(Throwable e) {
        super(e);
    }
}
