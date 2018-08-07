/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.exception;

import fr.whimtrip.ext.jwhtscrapper.annotation.WarningSign;
import fr.whimtrip.ext.jwhtscrapper.enm.Action;
import fr.whimtrip.ext.jwhtscrapper.enm.PausingBehavior;

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

    private Action action;

    private PausingBehavior pausingBehavior;

    public WarningSignException(Field field) {
        super("Warning sign triggered while doing setting the value to the field "
                + field.getDeclaringClass().getName() + "." + field.getName());

        WarningSign ws = field.getAnnotation(WarningSign.class);
        action = ws.action();
        pausingBehavior = ws.pausingBehavior();
    }

    public Action getAction() {
        return action;
    }

    public PausingBehavior getPausingBehavior() {
        return pausingBehavior;
    }
}
