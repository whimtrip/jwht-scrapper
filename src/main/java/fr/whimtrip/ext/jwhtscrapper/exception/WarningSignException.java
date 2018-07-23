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
 * Created by LOUISSTEIMBERG on 28/11/2017.
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
