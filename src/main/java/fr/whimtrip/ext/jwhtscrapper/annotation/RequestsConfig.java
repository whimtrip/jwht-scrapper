/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.annotation;

import fr.whimtrip.ext.jwhtscrapper.enm.PausingBehavior;
import fr.whimtrip.ext.jwhtscrapper.exception.RequestMaxRetriesReachedException;
import fr.whimtrip.ext.jwhtscrapper.service.RotatingUserAgent;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.DefaultAutomaticInnerScrapperClient;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.req.RequestSynchronizerImpl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     This annotation should be added to {@link Scrapper#requestsConfig()}
 *     and will define how HTTP requests should be performed to ensure
 *     the scrap is done properly and handles all possible edge cases which
 *     proves to be very very common when doing web scrapping on correctly
 *     protected websites.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface RequestsConfig {

    int DEFAULT_PARALLEL_THREADS = 20;
    int DEFAULT_WAIT_BETWEEN_REQUESTS = 200;
    int DEFAULT_TIMEOUT = 30_000;
    int DEFAULT_MAX_REQUEST_RETRIES = 6;
    int DEFAULT_WARNING_SIGN_DELAY = 120_000;

    /**
     * @return The max number of parrallel threads to run at the same time.
     *         You should start by testing it with quite low values and
     *         progressively increase it if no ban or errors comes out
     *         of your first tests.
     */
    int parallelThreads() default DEFAULT_PARALLEL_THREADS;

    /**
     * @return the minimum delay in milliseconds that must be waited between
     *         two requests. This parameter should be taken into account even
     *         though requests are made asynchronously and from several threads
     *         at a time. Current implementation uses {@link RequestSynchronizerImpl}
     *         to ensure that this is correctly respected.
     */
    int waitBetweenRequests() default DEFAULT_WAIT_BETWEEN_REQUESTS;

    /**
     * @return the request timeout in milliseconds. If you use proxies or if you
     *         poll slow websites/webpages, it is recommended to set it quite
     *         high altough you should test many setups, especially with proxies
     *         to find the best compromise between performances and request efficiency.
     */
    int timeout() default DEFAULT_TIMEOUT;

    /**
     * @return the maximum number of times a single request can be retried
     *         before an {@link RequestMaxRetriesReachedException} will be
     *         thrown. If using proxies, it is strongly advised to set this
     *         parameter to 20-30 as most commonly used proxies tends to work
     *         once every 10-20 times and once this exception is thrown, the
     *         current scrap cannot be retried.
     */
    int maxRequestRetries() default DEFAULT_MAX_REQUEST_RETRIES;

    /**
     * @return the delay in milliseconds on successful thread removal,
     *         {@link DefaultAutomaticInnerScrapperClient#removeFinishedThreads()}.
     *         This means that each time the scrapper client gather
     *         properly finished threads, it will pause for the given
     *         delay. 0 means it won't wait.
     */
    int periodicDelay() default 0;

    /**
     * @return the delay to wait for when a {@link WarningSign} is triggered.
     *         It will only work if {@link WarningSign#pausingBehavior()} is
     *         set to {@link PausingBehavior#PAUSE_ALL_THREADS} or
     *         {@link PausingBehavior#PAUSE_CURRENT_THREAD_ONLY}.
     * @see WarningSign#pausingBehavior()
     */
    int warningSignDelay() default DEFAULT_WARNING_SIGN_DELAY;

    /**
     * @return the proxy configuration. If no proxy will be used, simply
     *         set it to {@code @ProxyConfig()}. If using proxies, please
     *         see {@link ProxyConfig}.
     */
    ProxyConfig proxyConfig();

    /**
     * @return default cookies to use on each requests if any. Very
     *         useful for selecting a default currency and language
     *         that does not depends on the current proxy used which
     *         most of the times ends up quite poorly with regex for
     *         example. Most websites uses cookies for preferences.
     */
    Cookie[] defaultCookies() default {};

    /**
     * @return default headers to use on each request. Usually helpful
     *         for {@code Host} header for example.
     */
    Header[] defaultHeaders() default {};

    /**
     * @return default POST fields to use on POST requests only.
     */
    Field[] defaultPostFields() default {};

    /**
     * @return a boolean defining wether HTTP 301 and 302 redirections
     *         should or shouldn't be followed.
     */
    boolean followRedirections() default true;

    /**
     * @return a boolean defining wether redirections should or shouldn't
     *         be followed indefinitely. Sometimes, HTTP infinite redirections
     *         loops can happen and that's why the default value of this
     *         parameter is false. Will only be used if {@link #followRedirections()}
     *         returns true. Otherwise, it won't have any effect.
     */
    boolean allowInfiniteRedirections() default false;

    /**
     * @return a boolean indicating wether rotating user agent should be used or
     *         not. If set to true, {@link RotatingUserAgent#pickRandomUserAgent()}
     *         will be used to pick random user agent on each single request.
     *         Using rotating user agent will discard any {@code User-Agent}
     *         header set as default using {@link #defaultHeaders()}.
     */
    boolean rotatingUserAgent() default true;
}
