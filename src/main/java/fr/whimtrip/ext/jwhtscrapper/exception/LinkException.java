/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.exception;


import fr.whimtrip.ext.jwhtscrapper.annotation.Link;
import fr.whimtrip.ext.jwhtscrapper.annotation.LinkObject;
import fr.whimtrip.ext.jwhtscrapper.annotation.LinkObjects;
import fr.whimtrip.ext.jwhtscrapper.intfr.HtmlAutoScrapper;
import org.asynchttpclient.BoundRequestBuilder;

import java.lang.reflect.Field;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     Thrown when following links results into an exception.
 *     Link scoped exceptions are usually catched and
 *     thrown within the scope of the parent object
 *     {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}
 *     as this current exception class whose cause is the original
 *     exception.
 * </p>
 * <p>
 *     This exception can also be thrown if fields are not
 *     correctly annotated. see {@link Link} for more informations.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */

public class LinkException extends ScrapperException {

    public LinkException(String errorMessage) {
        super(errorMessage);
    }

    public LinkException(Throwable e)
    {
        this(e.getMessage());
        setStackTrace(e.getStackTrace());
    }

    public LinkException(Field field) {
        super(
                String.format(
                        "Field %s has a %s but isn't mapped to any object through %s or %s",
                        field, Link.class, LinkObject.class, LinkObjects.class
                )
        );

    }
}
