package fr.whimtrip.ext.jwhtscrapper.exception;

import java.lang.reflect.Field;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     Thrown when the url specified by a link from annotation
 *     configuration output a null / empty value.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class NullLinkException extends LinkException {
    public NullLinkException(Field field) {
        super(
                String.format(
                        "Field %s link cannot be followed because the link has a null value.",
                        field
                )
        );
    }
}
