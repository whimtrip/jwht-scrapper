



package fr.whimtrip.ext.jwhtscrapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by LOUISSTEIMBERG on 19/11/2017.
 */


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface HasLink {

}
