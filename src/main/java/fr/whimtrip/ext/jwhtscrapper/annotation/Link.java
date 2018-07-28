/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.annotation;

import fr.whimtrip.ext.jwhtscrapper.intfr.HttpRequestEditor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by LOUISSTEIMBERG on 19/11/2017.
 */


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface Link {

    public enum Method{
        GET,
        POST
    }

    Method method() default Method.GET;

    boolean editRequest() default false;

    Class<? extends HttpRequestEditor> requestEditor() default HttpRequestEditor.class;

    String regexCondition() default ".+";

    Field[] fields() default {};

    boolean followRedirections() default true;

}
