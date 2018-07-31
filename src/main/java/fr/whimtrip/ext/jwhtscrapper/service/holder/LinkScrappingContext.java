package fr.whimtrip.ext.jwhtscrapper.service.holder;


import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlAdapter;
import fr.whimtrip.ext.jwhtscrapper.exception.LinkException;
import fr.whimtrip.ext.jwhtscrapper.intfr.HtmlAutoScrapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.LinksFollower;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;



/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * <p>
 *     Inner holder class used by {@link LinksFollower} to prepare links
 *     to scrap and return them to {@link HtmlAutoScrapper}. It will hold
 *     the necessary context for a single link scrapping to take place.
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
public class LinkScrappingContext<P, U>
{
    private final BoundRequestBuilder boundRequestBuilder;
    private final U newObj;
    private final HtmlAdapter<U> adapter;
    private final Field fieldToBeSet;
    private final P parentObject;
    private final boolean followRedirections;
    private final boolean throwExceptions;

    /**
     * <p>Default Constructor for this context class.</p>
     * @param boundRequestBuilder see {@link #getBoundRequestBuilder()}
     * @param newObj see {@link #getNewObj()}
     * @param adapter see {@link #getAdapter()}
     * @param fieldToBeSet see {@link #getFieldToBeSet()}
     * @param parentObject see {@link #getParentObject()}
     * @param followRedirections see {@link #followRedirections()}
     * @param throwExceptions see {@link #throwExceptions()}
     */
    public LinkScrappingContext(
            @NotNull final BoundRequestBuilder boundRequestBuilder,
            @NotNull final U newObj,
            @NotNull final HtmlAdapter<U> adapter,
            @NotNull final Field fieldToBeSet,
            @NotNull final P parentObject,
                     final boolean followRedirections,
                     final boolean throwExceptions
    ){
        this.boundRequestBuilder = boundRequestBuilder;
        this.newObj = newObj;
        this.adapter = adapter;
        this.followRedirections = followRedirections;
        this.fieldToBeSet = fieldToBeSet;
        this.parentObject = parentObject;
        this.throwExceptions = throwExceptions;
    }

    /**
     * @return wether {@link LinkException} and all exceptions in the scope of the link scrapping
     *         processing should be followed or not.
     */
    public boolean throwExceptions() {

        return throwExceptions;
    }

    /**
     * @return the built in {@link BoundRequestBuilder} HTTP request preparator.
     */
    @NotNull
    public BoundRequestBuilder getBoundRequestBuilder() {

        return boundRequestBuilder;
    }

    /**
     * @return the newly retrieved object. At the moment it will be first retrieved (after preparation),
     * it should be only instanciated with field injection already performed.
     */
    @NotNull
    public U getNewObj() {

        return newObj;
    }

    /**
     * @return the {@link HtmlAdapter} to use for the child POJO fields parsing, analysing and mapping.
     */
    @NotNull
    public HtmlAdapter<U> getAdapter() {

        return adapter;
    }

    /**
     * @return Wether this link's HTTP redirections should be followed or not.
     */
    public boolean followRedirections() {

        return followRedirections;
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
