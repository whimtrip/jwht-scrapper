package fr.whimtrip.ext.jwhtscrapper.exception;

import fr.whimtrip.ext.jwhtscrapper.annotation.Link;

import java.lang.reflect.Field;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     Thrown when a field that is not String typed is incorrectly
 *     annotated with @{@link Link} annotation.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class LinkClassCastException extends LinkException {
    public LinkClassCastException(Field field) {
        super(
                String.format(
                        "Field %s link cannot be followed because any link provider field should" +
                        " hold a String typed instance.",
                        field
                )
        );
    }
}
