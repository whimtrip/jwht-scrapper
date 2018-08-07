/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.exception;

import fr.whimtrip.ext.jwhtscrapper.annotation.WarningSign;

import java.lang.reflect.Field;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     Triggered when a warning sign is detected.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class WarningSignException extends RequestFailedException {

    private WarningSign.Action action;

    public WarningSignException(Field field) {
        super("Warning sign triggered while doing setting the value to the field "
                + field.getDeclaringClass().getName() + "." + field.getName());

        action = field.getAnnotation(WarningSign.class).action();
    }

    public WarningSign.Action getAction() {
        return action;
    }
}
