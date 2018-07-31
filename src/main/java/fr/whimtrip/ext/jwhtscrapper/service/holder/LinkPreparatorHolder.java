/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service.holder;

import fr.whimtrip.ext.jwhtscrapper.annotation.Link;
import fr.whimtrip.ext.jwhtscrapper.exception.LinkException;
import fr.whimtrip.ext.jwhtscrapper.intfr.HttpRequestEditor;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.LinksFollowerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * <p>
 *     Inner holder class used by {@link LinksFollowerImpl}. It will hold a partial
 *     link preparation context so that it can be further sent as a simple object
 *     wether than a complex parameter agglomeration in corresponding methods.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class LinkPreparatorHolder<P> {

    private final P parent;

    private final String url;

    private final Link.Method method;

    private final List<PostField> fields;

    private final java.lang.reflect.Field parentField;

    private final Class<? extends HttpRequestEditor> requestEditorClazz;

    private final boolean followRedirections;

    private final boolean throwExceptions;

    /**
     * <p>Default constructor of this clas</p>
     * @param parent see {@link #getParent()}
     * @param url see {@link #getUrl()}
     * @param method see {@link #getMethod()}
     * @param fields see {@link #getFields()}
     * @param parentField see {@link #getParentField()}
     * @param requestEditorClazz see {@link #getRequestEditorClazz()}
     * @param followRedirections see {@link #followRedirections()}
     * @param throwExceptions see {@link #throwExceptions()}
     */
    public LinkPreparatorHolder(
            @NotNull  final P parent,
            @NotNull  final String url,
            @NotNull  final Link.Method method,
            @Nullable final List<PostField> fields,
            @NotNull  final java.lang.reflect.Field parentField,
            @NotNull  final Class<? extends HttpRequestEditor> requestEditorClazz,
                      final boolean followRedirections,
                      final boolean throwExceptions
    ){

        this.parent = parent;
        this.url = url;
        this.method = method;
        this.fields = fields;
        this.parentField = parentField;
        this.requestEditorClazz = requestEditorClazz;
        this.followRedirections = followRedirections;
        this.throwExceptions = throwExceptions;
    }

    /**
     * @return the parent POJO that should contain child POJO or list of
     *         child POJOs that will be preparated through this holder class.
     */
    public P getParent() {
        return parent;
    }

    /**
     * @return the url to scrap at.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the {@link Link.Method} HTTP method to use for the scrapping process.
     */
    public Link.Method getMethod() {
        return method;
    }

    /**
     * @return the list of {@link PostField} to add to the HTTP request if POST
     *         request.
     */
    public List<PostField> getFields() {
        return fields;
    }

    /**
     * @return the {@link HttpRequestEditor} class to use if any to modify and alter the
     *         request building process with custom processing unit.
     */
    public Class<? extends HttpRequestEditor> getRequestEditorClazz() {
        return requestEditorClazz;
    }

    /**
     * @return the parent field to set the resulting scrapped value to.
     */
    public java.lang.reflect.Field getParentField() {
        return parentField;
    }

    /**
     * @return wether HTTP redirections should be followed or not.
     */
    public boolean followRedirections() {
        return followRedirections;
    }

    /**
     * @return wether {@link LinkException} should be catched or thrown.
     */
    public boolean throwExceptions() {
        return throwExceptions;
    }

}
