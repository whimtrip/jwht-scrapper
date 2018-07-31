package fr.whimtrip.ext.jwhtscrapper.service.holder;


import fr.whimtrip.ext.jwhtscrapper.intfr.HtmlAutoScrapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.LinksFollower;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * <p>
 *     Inner holder class used by {@link LinksFollower} to prepare links
 *     to scrap and return them to {@link HtmlAutoScrapper}. It will hold
 *     the necessary context for a list of links to be followed and scrapped
 *     and assigned to a single List field.
 * </p>
 *
 * <p>
 *     This class therefore extends {@link ArrayList} with {@link LinkListScrappingContext}
 *     typed List. It further contains the {@link #getFieldToBeSet()} and {@link #getParentObject()}
 *     so that they can be retrieved from this instance and not from any underlying
 *     {@link LinkListScrappingContext} which would lead to potential pattern flaws.
 * </p>
 *
 * @param <P> the parent POJO type.
 * @param <U> the child POJO type which is either the type of the field to set
 *            or, if an instance of this scrapping context is wrapped around a
 *           {@link LinkListScrappingContext}, the type of the List field to
 *           populate.
 * @author Louis-wht
 * @since 1.0.0
 */
public class LinkListScrappingContext<P, U> extends ArrayList<LinkScrappingContext<P, U>> {

    @NotNull
    private final Field fieldToBeSet;
    @NotNull
    private final P parentObject;

    /**
     * <p>Default Constructor of this list class.</p>
     * @param fieldToBeSet see {@link #getFieldToBeSet()}
     * @param parentObject see {@link #getParentObject()}
     */
    public LinkListScrappingContext(@NotNull final Field fieldToBeSet, @NotNull final P parentObject) {
        super();
        this.fieldToBeSet = fieldToBeSet;
        this.parentObject = parentObject;
    }

    /**
     * @return the field to set the resulting value to.
     */
    @NotNull
    public Field getFieldToBeSet() {

        return fieldToBeSet;
    }

    /**
     * @return the parent Object to assign {@link #getFieldToBeSet()} the resulting value of the current scrap.
     */
    @NotNull
    public P getParentObject() {

        return parentObject;
    }
}
