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
@Target({ElementType.FIELD })
public @interface RequestsConfig {

    int DEFAULT_PARALLEL_THREADS = 20;
    int DEFAULT_WAIT_BETWEEN_REQUESTS = 200;
    int DEFAULT_TIMEOUT = 30_000;
    int DEFAULT_MAX_REQUEST_RETRIES = 6;
    int DEFAULT_WARNING_SIGN_DELAY = 120_000;

    int parallelThreads() default DEFAULT_PARALLEL_THREADS;

    int waitBetweenRequests() default DEFAULT_WAIT_BETWEEN_REQUESTS;

    int timeout() default DEFAULT_TIMEOUT;

    int maxRequestRetries() default DEFAULT_MAX_REQUEST_RETRIES;

    int periodicDelay() default 0;

    int warningSignDelay() default DEFAULT_WARNING_SIGN_DELAY;

    ProxyConfig proxyConfig();

    Cookie[] defaultCookies() default {};

    Header[] defaultHeaders() default {};

    Field[] defaultPostFields() default {};

    boolean followRedirections() default true;

    boolean rotatingUserAgent() default true;

    boolean allowInfiniteRedirections() default false;
}
