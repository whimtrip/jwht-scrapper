



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
 *     This annotation can be applied to any <strong>List of child POJO</strong> or
 *     <strong>child POJO</strong> typed field of a parent POJO.
 * </p>
 *
 * <p>
 *     It will enable link scanning (through {@link Link} and {@link LinkListsFromBuilder}
 *     annotated fields search) to go one layer deeper in your POJOs arborescence so
 *     that your child POJOs can have their own links and child scrappings. Without
 *     this annotation present on to of the child POJO typed field of the parent POJO,
 *     the child POJO won't be investigated and therefore its links won't be followed.
 * </p>

 * @see Link
 * @see LinkObject
 * @author Louis-wht
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface HasLink {

}
