/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by LOUISSTEIMBERG on 28/11/2017.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface WarningSign {

    enum TriggeredOn {
        NULL_VALUE,
        DEFAULT_VALUE,
        ANY_CORRECT_VALUE,
        ANY_VALUE_MATCHING_REGEX,
        ANY_VALUE_NOT_MATCHING_REGEX
    }

    enum Action{
        RETRY,
        THROW_EXCEPTION,
        STOP_ACTUAL_SCRAP
    }

    /**
     * @return Defines in which case the request should be done once again
     */
    TriggeredOn triggeredOn();

    /**
     * @return The regex value that will be used to runDuringTests for the retry on annotation
     */
    String triggeredOnRegex() default "";

    Action action() default Action.RETRY;
}
