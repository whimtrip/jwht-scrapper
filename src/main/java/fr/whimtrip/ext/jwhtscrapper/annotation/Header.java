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
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     This annotation is thought to be applied in {@link RequestsConfig#defaultHeaders()}.
 *     It's a simple HTTP header annotation based representation.
 * </p>
 * @see Link
 * @see LinkObject
 * @author Louis-wht
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface Header {

    /**
     * @return the name of the HTTP header represented.
     */
    String name();

    /**
     * @return the value of the HTTP header represented.
     */
    String value();
}
