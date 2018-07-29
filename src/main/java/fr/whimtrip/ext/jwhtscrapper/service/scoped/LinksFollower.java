package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.core.util.WhimtripUtils;
import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhthtmltopojo.adapter.HtmlToPojoAnnotationMap;
import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlAdapter;
import fr.whimtrip.ext.jwhtscrapper.annotation.HasLink;
import fr.whimtrip.ext.jwhtscrapper.annotation.Link;
import fr.whimtrip.ext.jwhtscrapper.annotation.LinkListsFromBuilder;
import fr.whimtrip.ext.jwhtscrapper.exception.LinkClassCastException;
import fr.whimtrip.ext.jwhtscrapper.exception.LinkException;
import fr.whimtrip.ext.jwhtscrapper.exception.LinkListCastException;
import fr.whimtrip.ext.jwhtscrapper.exception.NullLinkException;
import fr.whimtrip.ext.jwhtscrapper.intfr.HttpRequestEditor;
import fr.whimtrip.ext.jwhtscrapper.intfr.LinkListFactory;
import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkListScrappingContext;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkPreparatorHolder;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkScrappingContext;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static fr.whimtrip.ext.jwhtscrapper.annotation.Link.DEFAULT_REGEX_COND;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class LinksFollower {

    private static final Logger log = LoggerFactory.getLogger(LinksFollower.class);


    private final HttpManagerClient httpManagerClient;
    private final HtmlToPojoEngine htmlToPojoEngine;
    private final Object model;
    private final HtmlAdapter adapter;
    private final ExceptionLogger exceptionLogger;

    private final List<LinkScrappingContext> scrappingContexts = new ArrayList<>();
    private final List<LinkListScrappingContext> linkListsScrappingContexts = new ArrayList<>();

    public <P> LinksFollower(HttpManagerClient httpManagerClient, HtmlToPojoEngine htmlToPojoEngine, ExceptionLogger exceptionLogger, P model, HtmlAdapter<P> adapter) {

        this.httpManagerClient = httpManagerClient;
        this.htmlToPojoEngine = htmlToPojoEngine;
        this.exceptionLogger = exceptionLogger;
        this.model = model;
        this.adapter = adapter;
    }

    @SuppressWarnings("unchecked")
    public <U> void resolveBasicLinks() throws  LinkException {
        resolveBasicLinks((U)model, (HtmlAdapter<U>) adapter);
    }

    public List<LinkScrappingContext> getScrappingContexts() {

        return scrappingContexts;
    }

    public List<LinkListScrappingContext> getLinkListsScrappingContexts() {

        return linkListsScrappingContexts;
    }

    private <P, U> void resolveBasicLinks(P model, HtmlAdapter<P> adapter) throws LinkException {

        log.info("Resolving basic links for model type " + model.getClass());

        List<HtmlToPojoAnnotationMap<Link>> links = adapter.getFieldList(Link.class);
        List<HtmlToPojoAnnotationMap<HasLink>> hasLinks = adapter.getFieldList(HasLink.class);
        List<HtmlToPojoAnnotationMap<LinkListsFromBuilder>> linkListsFromBuilders = adapter.getFieldList(LinkListsFromBuilder.class);


        resolveChildPojosLinks(model, hasLinks);

        resolveListLinks(model, linkListsFromBuilders);

        followLinks(links, model, adapter);
    }

    private <P, U> void resolveListLinks(P model, List<HtmlToPojoAnnotationMap<LinkListsFromBuilder>> linkListsFromBuilders) throws LinkException {

        for(HtmlToPojoAnnotationMap<LinkListsFromBuilder> linkList : linkListsFromBuilders)
        {
            if(List.class.isAssignableFrom(linkList.getField().getType())) {

                Type genericType = linkList.getField().getGenericType();
                Type type = ((ParameterizedType) genericType).getActualTypeArguments()[0];

                LinkListScrappingContext<P, U> ulist = followLinkLists(linkList, model, htmlToPojoEngine.adapter((Class<U>) type));
                linkListsScrappingContexts.add(ulist);

            }

            else throw new LinkListCastException(linkList.getField());
        }
    }

    private <P, U> void resolveChildPojosLinks(P model, List<HtmlToPojoAnnotationMap<HasLink>> hasLinks) throws LinkException {

        for(HtmlToPojoAnnotationMap<HasLink> hasLink : hasLinks)
        {
            if(Collection.class.isAssignableFrom(hasLink.getField().getType()))
            {
                try {
                    Collection<U> list = WhimtripUtils.getObjectFromField(hasLink.getField(), model);
                    for(U element : list)
                    {
                        resolveBasicLinks(element, (HtmlAdapter<U>) htmlToPojoEngine.adapter(element.getClass()));
                    }

                }
                catch(IllegalAccessException e)
                {
                    e.printStackTrace();
                    throw new LinkException(e);
                }

            }
            else
            {
                try {
                    U element = WhimtripUtils.getObjectFromField(hasLink.getField(), model);
                    resolveBasicLinks(element,(HtmlAdapter<U>) htmlToPojoEngine.adapter(element.getClass()));
                }
                catch(IllegalAccessException e)
                {
                    e.printStackTrace();
                    throw new LinkException(e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <U, P> void followLinks(List<HtmlToPojoAnnotationMap<Link>> links, U model, HtmlAdapter<U> adapter) throws LinkException {

        for (HtmlToPojoAnnotationMap<Link> link : links) {
            Field objField = ((ScrapperHtmlAdapter<P>) adapter).getLinkObject(link);

            if (objField == null) throw new LinkException(link.getField());

            String url = getLinkUrl(link);

            if(checkRegexCondition(link, url))
            {

                HtmlAdapter<U> newFieldAdapter = (HtmlAdapter<U>) htmlToPojoEngine.adapter(objField.getType());

                boolean editRequest = link.getAnnotation().editRequest();

                LinkPreparatorHolder container =
                        new LinkPreparatorHolder()
                                .buildFields(link.getAnnotation().fields())
                                .setFollowRedirections(link.getAnnotation().followRedirections())
                                .setThrowExceptions(link.getAnnotation().throwExceptions())
                                .setParentField(objField)
                                .setParent(model)
                                .setMethod(link.getAnnotation().method())
                                .setRequestEditorClazz(link.getAnnotation().requestEditor())
                                .setUrl(url);

                HttpRequestEditor<P, U> requestEditor = null;
                if (editRequest)
                {
                    requestEditor =
                            (HttpRequestEditor<P, U>)
                                    (WhimtripUtils.createNewInstance(container.getRequestEditorClazz()));


                    requestEditor.init(container.getParentField());

                }

                scrappingContexts.add(buildContext(container, newFieldAdapter, requestEditor, editRequest));
            }

        }
    }

    private boolean checkRegexCondition(HtmlToPojoAnnotationMap<Link> link, String linkVal){

        if(linkVal == null)
            return false;

        if(link.getAnnotation().regexCondition().equals(DEFAULT_REGEX_COND))
            return true;

        Pattern pattern = Pattern.compile(link.getAnnotation().regexCondition());

        return pattern.matcher(linkVal).find();
    }

    @Nullable
    private String getLinkUrl(@NotNull final HtmlToPojoAnnotationMap<Link> link) throws LinkException {

        String linkVal = null;

        try{
            Object linkRawVal = WhimtripUtils.getObjectFromField(link.getField(), model);

            if(linkRawVal == null)
                throw new NullLinkException(link.getField());

            if(!(linkRawVal instanceof String))
                throw new LinkClassCastException(link.getField());

            linkVal = (String) linkRawVal;
        }
        catch(IllegalAccessException e)
        {
            throw new LinkException(e);
        }
        catch (LinkException e) {
            if(link.getAnnotation().throwExceptions())
                throw e;
            exceptionLogger.logException(e);
        }
        return linkVal;
    }


    @NotNull
    @SuppressWarnings("unchecked")
    private <P, U> LinkListScrappingContext<P, U> followLinkLists(
            @NotNull final HtmlToPojoAnnotationMap<LinkListsFromBuilder> links,
            @NotNull final P parent,
            @NotNull final HtmlAdapter<U> adapter
    ){
        LinkListFactory<P> listFactory = WhimtripUtils.createNewInstance(links.getAnnotation().value());
        LinkListScrappingContext<P, U> ulist = new LinkListScrappingContext<>(links.getField(), parent);

        List<LinkPreparatorHolder> containers = listFactory.createLinkPreparatorLists(parent, links.getField());

        HttpRequestEditor<P, U> requestEditor = null;

        for(LinkPreparatorHolder cntn : containers)
        {

            boolean editRequest = links.getAnnotation().editRequest();

            if (editRequest && requestEditor == null)
            {
                requestEditor =
                        (HttpRequestEditor<P, U>)
                                (WhimtripUtils.createNewInstance(cntn.getRequestEditorClazz()));


                requestEditor.init(cntn.getParentField());
            }

            LinkScrappingContext<P, U> newObjCntx =
                    (LinkScrappingContext<P, U>)
                            buildContext(cntn, adapter, requestEditor, editRequest);

            if (newObjCntx != null)
                ulist.add(newObjCntx);
        }

        return ulist;

    }


    @Nullable
    private <P, U> LinkScrappingContext<P, U> buildContext(
                            @NotNull  final LinkPreparatorHolder<P> container,
                            @NotNull  final HtmlAdapter<U> adapter,
                            @Nullable final HttpRequestEditor<P, U> requestEditor,
                                      final boolean editRequest
    ){

        if (!editRequest || requestEditor.doRequest(container.getParent()))
        {
            U newObj = adapter.createNewInstance(container.getParent());

            BoundRequestBuilder req = buildReq(container, requestEditor, newObj, editRequest);


            return
                    new LinkScrappingContext<>(
                            req,
                            newObj,
                            adapter,
                            container.getParentField(),
                            container.getParent(),
                            container.followRedirections(),
                            container.throwExceptions()
                    );
        }

        return null;
    }

    private <P, U> BoundRequestBuilder buildReq(
            @NotNull  final LinkPreparatorHolder<P> container,
            @Nullable final HttpRequestEditor<P, U> requestEditor,
            @Nullable final U newObj,
                      final boolean editRequest
    ){

        if (editRequest && requestEditor != null)
            requestEditor.prepareObject(newObj, container.getParent(), container);


        BoundRequestBuilder req;

        if (container.getMethod() == Link.Method.GET)
        {
            req = httpManagerClient.prepareGet(container.getUrl());
        }
        else
        {
            req = httpManagerClient.preparePost(container.getUrl());

            for(Map.Entry<String, Object> fieldEntr: container.getFields().entrySet())
            {
                req.addFormParam(fieldEntr.getKey(), fieldEntr.getValue().toString());
            }
        }

        if (editRequest)
            requestEditor.editRequest(req, container);
        return req;
    }


}
