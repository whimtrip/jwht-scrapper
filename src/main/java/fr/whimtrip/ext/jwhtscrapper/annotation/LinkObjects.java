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
 *     This annotation can be applied to any <strong>List of POJO</strong>
 *     typed field of a POJO.
 * </p>
 *
 * <p>
 *     All {@link Link} annotated fields whose field names will be in
 *     {@link #value()} field of this annotation will be mapped and
 *     added to the {@link LinkObjects} annotated list field.
 * </p>
 *
 * @see Link
 * @see LinkObject
 * @author Louis-wht
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface LinkObjects {

    /**
     * @return the names of the {@link Link} annotated fields of your POJO
     *         to scrap, map and add to the current {@link LinkObjects}
     *         annotated POJO list field.
     */
    String[] value();

}
