/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.annotation;

import fr.whimtrip.ext.jwhtscrapper.enm.Method;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     This annotation can be applied to your {@link ScrapperHelper}
 *     implementation. Together and with the POJOs properly annotated
 *     to reflect the web pages to scrap, they will represent your
 *     basic setup to get you started with your scrapping operation.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
public @interface Scrapper {

    int DEFAULT_SCRAP_LIMIT = 50;

    /**
     * @return the model parent POJO class on which you will map each
     *         original scrapped url. Those POJOs can have child POJOs
     *         as defined by jwht-htmltopojo library. Child POJOs can
     *         also be populated using Linking feature available only
     *         with this current library.
     * @see Link
     * @see LinkListsFromBuilder
     */
    Class<?> scrapModel();

    /**
     * @return the HTTP method to use.
     */
    Method method() default Method.GET;

    /**
     * @return the request configuration.
     * @see RequestsConfig
     */
    RequestsConfig requestConfig();

    /**
     * @return wether scrapping exceptions should be ignored {@code false}
     *         or thrown to stop the whole scrapping process.
     */
    boolean throwExceptions() default true;

    /**
     * @return the scrapping limit which defines the maximum number
     *         of starting pages to scrap. This can prove to be very
     *         useful when testing your scrapper configurations and
     *         POJOs.
     */
    int scrapLimit() default DEFAULT_SCRAP_LIMIT;
}
