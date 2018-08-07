/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.annotation;

import fr.whimtrip.ext.jwhthtmltopojo.annotation.Selector;
import fr.whimtrip.ext.jwhtscrapper.enm.Action;
import fr.whimtrip.ext.jwhtscrapper.enm.PausingBehavior;
import fr.whimtrip.ext.jwhtscrapper.enm.TriggeredOn;
import fr.whimtrip.ext.jwhtscrapper.intfr.BasicObjectMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     This annotation can be applied to any field of a POJO that will be
 *     used to map any HTML response body to it.
 * </p>
 * <p>
 *     This allows to defines some websites areas (such as "Are You A Robot"
 *     Google's captcha) that are not supposed to show up in normal scrapping
 *     conditions. When and if this areas are detected, special actions can
 *     be taken and are defined by {@link Action}. The pausing behavior can
 *     be choosen using {@link PausingBehavior}, and the triggering behavior
 *     can be choosen using {@link TriggeredOn} and eventually {@link #triggeredOnRegex()}.
 * </p>
 *
 * <p>
 *     This way, when for example Google Captcha is detected, you can program
 *     your scrapper to wait for 5 minutes (using {@link RequestsConfig#warningSignDelay()}
 *     on all running threads.
 * </p>
 *
 * <strong>
 *     Warning! This annotation only works with full usage of jwht-htmltopojo and
 *     no other {@link BasicObjectMapper} provided. Therefore, you have to use it
 *     for HTML to POJO mapping, which means it must be used when scrapping web
 *     standard pages. You will need to use an {@link Selector} annotation from
 *     jwht-htmltopojo in order for this Warning Sign to work properly. Otherwise
 *     most chances are that it won't either be triggered or will fail because of
 *     uncaught and unexpected exceptions.
 * </strong>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface WarningSign {

    /**
     * @see TriggeredOn
     * @see Selector
     * @return the type of trigger that will reveal a warning sign. It works
     *         and requires usage of {@link Selector} annotation from jwht-htmltopojo
     *         library.
     */
    TriggeredOn triggeredOn();

    /**
     * @return The regex value that will be used to check the warning sign if the
     *         {@link #triggeredOn()} is a regex type trigger.
     */
    String triggeredOnRegex() default "";

    /**
     * @see Action
     * @return the type of action to perform on warning sign triggering.
     */
    Action action() default Action.RETRY;

    /**
     * @see PausingBehavior
     * @return the pausing behavior to adopt on warning sign triggering.
     */
    PausingBehavior pausingBehavior() default PausingBehavior.PAUSE_ALL_THREADS;
}
