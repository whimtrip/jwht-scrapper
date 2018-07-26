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

import java.lang.reflect.Field; /**
 * Created by LOUISSTEIMBERG on 19/11/2017.
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
