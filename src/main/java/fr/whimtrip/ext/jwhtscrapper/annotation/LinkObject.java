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
 *     This annotation can be applied to any <strong>child POJO</strong>
 *     typed field of a POJO.
 * </p>
 *
 * <p>
 *     {@link Link} annotated field whose field name is equal to
 *     {@link #value()} field of this annotation will be scrapped,
 *     mapped and set to the current {@link LinkObject} annotated
 *     field.
 * </p>
 *
 * @see Link
 * @see LinkObjects
 * @author Louis-wht
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface LinkObject {


    /**
     * @return the name of the {@link Link} annotated field of your POJO
     *         to scrap, map and set to the current {@link LinkObjects}
     *         annotated child POJO field.
     */
    String value() default "link";
}
