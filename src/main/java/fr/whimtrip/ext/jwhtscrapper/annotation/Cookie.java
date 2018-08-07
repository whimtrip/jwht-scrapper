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
 *     This annotation is used {@link RequestsConfig#defaultCookies()}  here}.
 *     It represents a standard HTTP cookie.
 * </p>
 *
 * @see Link
 * @see LinkObject
 * @author Louis-wht
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface Cookie {

    /**
     * @return the cookie name.
     */
    String name();

    /**
     * @return the cookie value.
     */
    String value();

    /**
     * @return the cookie domain.
     */
    String domain();

    /**
     * @return the cookie path.
     */
    String path() default "/";
}
