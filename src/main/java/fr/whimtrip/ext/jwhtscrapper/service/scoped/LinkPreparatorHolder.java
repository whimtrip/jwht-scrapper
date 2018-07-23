/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.ext.jwhtscrapper.annotation.Link;
import fr.whimtrip.ext.jwhtscrapper.annotation.LinkField;
import fr.whimtrip.ext.jwhtscrapper.intfr.HttpRequestEditor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LOUISSTEIMBERG on 19/11/2017.
 */
public class LinkPreparatorHolder<P> {

    private P parent;

    private String url;

    private Link.Method method;

    private Map<String, Object> fields = new HashMap<>();

    private Field parentField;

    private boolean followRedirections;

    private Class<? extends HttpRequestEditor> requestEditorClazz;

    public P getParent() {
        return parent;
    }

    public LinkPreparatorHolder setParent(P parent) {
        this.parent = parent;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public LinkPreparatorHolder setUrl(String url) {
        this.url = url;
        return this;
    }

    public Link.Method getMethod() {
        return method;
    }

    public LinkPreparatorHolder setMethod(Link.Method method) {
        this.method = method;
        return this;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public LinkPreparatorHolder setFields(Map<String, Object> fields) {
        this.fields = fields;
        return this;
    }

    public Class<? extends HttpRequestEditor> getRequestEditorClazz() {
        return requestEditorClazz;
    }

    public LinkPreparatorHolder setRequestEditorClazz(Class<? extends HttpRequestEditor> requestEditorClazz) {
        this.requestEditorClazz = requestEditorClazz;
        return this;
    }

    public Field getParentField() {
        return parentField;
    }

    public LinkPreparatorHolder setParentField(Field parentField) {
        this.parentField = parentField;
        return this;
    }

    public LinkPreparatorHolder buildFields(LinkField[] fields) {
        for(LinkField field : fields)
        {
            this.fields.put(field.name(), field.value());
        }
        return this;
    }

    public boolean isFollowRedirections() {
        return followRedirections;
    }

    public LinkPreparatorHolder setFollowRedirections(boolean followRedirections) {
        this.followRedirections = followRedirections;
        return this;
    }
}
