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
 * Created by LOUISSTEIMBERG on 21/11/2017.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
public @interface Scrapper {

    int DEFAULT_SCRAP_LIMIT = 50;

    Class<?> scrapModel();

    Link.Method method() default Link.Method.GET;

    RequestsConfig requestConfig();

    boolean throwExceptions() default true;

    int scrapLimit() default DEFAULT_SCRAP_LIMIT;
}
