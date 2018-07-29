package fr.whimtrip.ext.jwhtscrapper.service.holder;


import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class LinkListScrappingContext<P, U> extends ArrayList<LinkScrappingContext<P, U>> {

    @NotNull
    private final Field fieldToBeSet;
    @NotNull
    private final P parentObject;

    public LinkListScrappingContext(@NotNull final Field fieldToBeSet, @NotNull final P parentObject) {
        super();
        this.fieldToBeSet = fieldToBeSet;
        this.parentObject = parentObject;
    }

    @NotNull
    public Field getFieldToBeSet() {

        return fieldToBeSet;
    }

    @NotNull
    public P getParentObject() {

        return parentObject;
    }
}
