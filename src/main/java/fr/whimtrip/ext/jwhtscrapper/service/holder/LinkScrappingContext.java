package fr.whimtrip.ext.jwhtscrapper.service.holder;


import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlAdapter;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class LinkScrappingContext<P, U>
{
    private final BoundRequestBuilder boundRequestBuilder;
    private final U newObj;
    private final HtmlAdapter<U> adapter;
    private final Field fieldToBeSet;
    private final P parentObject;
    private final boolean followRedirections;
    private final boolean throwExceptions;

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

    public boolean throwExceptions() {

        return throwExceptions;
    }

    public BoundRequestBuilder getBoundRequestBuilder() {

        return boundRequestBuilder;
    }

    public U getNewObj() {

        return newObj;
    }

    public HtmlAdapter<U> getAdapter() {

        return adapter;
    }

    public boolean followRedirections() {

        return followRedirections;
    }

    public Field getFieldToBeSet() {

        return fieldToBeSet;
    }

    public P getParentObject() {

        return parentObject;
    }
}