/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.annotation;

import fr.whimtrip.ext.jwhtscrapper.enm.Method;
import fr.whimtrip.ext.jwhtscrapper.intfr.HttpRequestEditor;
import fr.whimtrip.ext.jwhtscrapper.intfr.LinksFollower;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     This annotation can be applied to any <strong>String</strong> field of
 *     a POJO that will be used to map any HTTP response body to it.
 * </p>
 *
 * <p>
 *     Any {@link Link} field defines a field that will contain an
 *     url to scrap. The result of the scrap should be assigned to
 *     the field of the same POJO annotated with {@link LinkObject}
 *     or {@link LinkObjects} where {@link LinkObject#value()}
 *     is equal to the {@link Link} annotated field name or where
 *     {@link LinkObjects#value()} contains the {@link Link} annotated
 *     field name.
 * </p>
 *
 * <p>
 *     If linked to an {@link LinkObject} field, the resulting value
 *     will be directly set to the {@link LinkObject} annotated field.
 * </p>
 *
 * <p>
 *     If linked to an {@link LinkObjects} field, the resulting value
 *     will be added to the {@link LinkObjects} annotated field List
 *     instance.
 * </p>
 *
 * <p>
 *     <strong>
 *         If the current POJO is not the parent POJO that will directly
 *         be mapped from raw HTTP response body to POJO but a child POJO,
 *         you must annotate the parent POJO field containing your child
 *         {@link Link} annotated POJO or a list of it with {@link HasLink}
 *         annotation so that {@link LinksFollower} will be able to further
 *         analyse your child POJO recursively for links to scrap.
 *     </strong>
 * </p>
 *
 * @see HasLink
 * @see LinkObject
 * @see LinkObjects
 * @see LinkListsFromBuilder
 * @author Louis-wht
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface Link {

    String DEFAULT_REGEX_COND = ".+";

    /**
     * @return the method to use to follow the link.
     */
    Method method() default Method.GET;

    /**
     * @return a boolean indicating if the request should be followed or not.
     *         If it returns {@code true}, a custom {@link HttpRequestEditor}
     *         implementation should be set through {@link #requestEditor()}.
     */
    boolean editRequest() default false;

    /**
     * @return the request editor class to instanciate in order to tune the
     *         request with user defined processing unit.
     *         <strong>
     *             Will only be used if {@link #editRequest()} returns {@code true}.
     *         </strong>
     */
    Class<? extends HttpRequestEditor> requestEditor() default HttpRequestEditor.class;

    /**
     * @return regex condition the link url should match to be followed.
     *         If not matching this regex, the link won't be followed.
     *         Default regex condition will match all strings.
     */
    String regexCondition() default DEFAULT_REGEX_COND;

    /**
     * @return the default post fields to add to this HTTP request.
     */
    Field[] fields() default {};

    /**
     * @return wether the HTTP 301 and 302 redirections should be followed.
     *         It will work independantly from global configuration set
     *         {@link RequestsConfig#followRedirections() here}.
     * @see RequestsConfig#followRedirections()
     */
    boolean followRedirections() default true;

    /**
     * @return wether exceptions should be thrown or not when following
     *         this link. It also works independantly from global configuration
     *         set {@link Scrapper#throwExceptions() here}.
     * @see Scrapper#throwExceptions()
     */
    boolean throwExceptions() default false;

}
