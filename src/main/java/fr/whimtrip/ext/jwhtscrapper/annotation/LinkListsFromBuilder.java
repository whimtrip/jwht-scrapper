/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.annotation;

import fr.whimtrip.ext.jwhtscrapper.intfr.HttpRequestEditor;
import fr.whimtrip.ext.jwhtscrapper.intfr.LinkListFactory;
import fr.whimtrip.ext.jwhtscrapper.intfr.LinksFollower;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkPreparatorHolder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     This annotation can be applied to any <strong>List of POJO</strong>
 *     typed field of a POJO.
 * </p>
 *
 * <p>
 *     The {@link #value()} will give the factory class to instanciate
 *     in order to prepare all {@link LinkPreparatorHolder} to scrap
 *     links with and map results to child POJOs. Those child POJOs
 *     instances will be added to the current list field.
 * </p>
 *
 * <p>
 *     <strong>
 *         If the current POJO is not the parent POJO that will directly
 *         be mapped from raw HTTP response body to POJO but a child POJO,
 *         you must annotate the parent POJO field containing your child
 *         {@link LinkListsFromBuilder} annotated POJO or a list of it with
 *         {@link HasLink} annotation so that {@link LinksFollower} will be
 *         able to further analyse your child POJO recursively for links to
 *         scrap.
 *     </strong>
 * </p>
 *
 * @see HasLink
 * @see Link
 * @author Louis-wht
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface LinkListsFromBuilder {

    /**
     * @return the user-defined {@link LinkListFactory} class that will
     *         be instanciated and further used to generate all
     *         {@link LinkPreparatorHolder} to scrap links with and map
     *         results to child POJOs. Those child POJOs instances will
     *         be added to the current list field.
     */
    Class<? extends LinkListFactory> value();

    /**
     * @return wether requests should be edited with user-defined custom
     *         {@link HttpRequestEditor} implementation. If set to true,
     *         the {@link HttpRequestEditor} class can be provided while
     *         instanciating the {@link LinkPreparatorHolder}. See
     *         {@link LinkPreparatorHolder#getRequestEditorClazz()} for
     *         further information.
     */
    boolean editRequest() default true;

}
