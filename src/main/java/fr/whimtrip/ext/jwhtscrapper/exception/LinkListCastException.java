package fr.whimtrip.ext.jwhtscrapper.exception;

import fr.whimtrip.ext.jwhtscrapper.annotation.LinkListsFromBuilder;

import java.lang.reflect.Field;
import java.util.List;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class LinkListCastException extends LinkException {
    public LinkListCastException(Field field) {
        super(
                String.format(
                        "%s cannot be used on a non %s field for field %s.",
                        LinkListsFromBuilder.class.getName(),
                        List.class.getName(),
                        field
                )
        );

    }
}
