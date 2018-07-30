package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.whimtrip.core.util.WhimtripUtils;
import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhthtmltopojo.adapter.HtmlToPojoAnnotationMap;
import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlAdapter;
import fr.whimtrip.ext.jwhtscrapper.annotation.HasLink;
import fr.whimtrip.ext.jwhtscrapper.annotation.Link;
import fr.whimtrip.ext.jwhtscrapper.annotation.LinkListsFromBuilder;
import fr.whimtrip.ext.jwhtscrapper.annotation.LinkObject;
import fr.whimtrip.ext.jwhtscrapper.annotation.LinkObjects;
import fr.whimtrip.ext.jwhtscrapper.exception.LinkClassCastException;
import fr.whimtrip.ext.jwhtscrapper.exception.LinkException;
import fr.whimtrip.ext.jwhtscrapper.exception.LinkListCastException;
import fr.whimtrip.ext.jwhtscrapper.exception.NullLinkException;
import fr.whimtrip.ext.jwhtscrapper.intfr.HtmlAutoScrapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.HttpRequestEditor;
import fr.whimtrip.ext.jwhtscrapper.intfr.LinkListFactory;
import fr.whimtrip.ext.jwhtscrapper.intfr.LinksFollower;
import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkListScrappingContext;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkPreparatorHolder;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkScrappingContext;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;

import static fr.whimtrip.ext.jwhtscrapper.annotation.Link.DEFAULT_REGEX_COND;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     Default implementation of {@link LinksFollower} interface that
 *     respect all of its contracts and prerequisite.
 * </p>
 *
 * @see LinksFollower
 * @author Louis-wht
 * @since 1.0.0
 */
public final class LinksFollowerImpl implements LinksFollower {

    private static final Logger log = LoggerFactory.getLogger(LinksFollowerImpl.class);


    private final HttpManagerClient httpManagerClient;
    private final HtmlToPojoEngine htmlToPojoEngine;
    private final Object model;
    private final HtmlAdapter adapter;
    private final ExceptionLogger exceptionLogger;

    private final List<LinkScrappingContext> scrappingContexts = new ArrayList<>();
    private final List<LinkListScrappingContext> linkListsScrappingContexts = new ArrayList<>();
    private final Map<Field, LinkListScrappingContext> mappedLinkedListsScrappingContexts = new HashMap<>();


    private boolean resolved = false;

    /**
     *
     * <p>Default Public constructor of this class.</p>
     * @param httpManagerClient the {@link HttpManagerClient} to be used to prepare
     *                          {@link BoundRequestBuilder} as one of the last steps
     *                          of the links following process, using method
     *                          {@link HttpManagerClient#prepareGet(String)} or
     *                          {@link HttpManagerClient#preparePost(String)}.<br>
     * @param htmlToPojoEngine the htmlToPojoEngine that will be used here only
     *                         to perform POJO class fields analysis. The mapping
     *                         will be triggered from the {@link HtmlAutoScrapper}
     *                         and therefore still support custom {@link ObjectMapper}
     *                         implementation for other input formats conversion.<br>
     * @param exceptionLogger the {@link ExceptionLogger} to use to log potentially thrown
     *                        and catched exceptions if any.<br>
     * @param model the original parent model to assign fields to.<br>
     * @param adapter the parent POJO adapter that will also be used to retrieve
     *                child POJO adapters.<br>
     * @param <P> the type of both the parent model and the parent adapter.
     */
    public <P> LinksFollowerImpl(
            @NotNull final HttpManagerClient httpManagerClient,
            @NotNull final HtmlToPojoEngine htmlToPojoEngine,
            @NotNull final ExceptionLogger exceptionLogger,
            @NotNull final P model,
            @NotNull final HtmlAdapter<P> adapter
    ){

        this.httpManagerClient = httpManagerClient;
        this.htmlToPojoEngine = htmlToPojoEngine;
        this.exceptionLogger = exceptionLogger;
        this.model = model;
        this.adapter = adapter;
    }

    /**
     * @see LinksFollower#resolveBasicLinks()
     * @throws LinkException see {@link LinksFollower#resolveBasicLinks()}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void resolveBasicLinks() throws  LinkException {

        if(resolved)
            return;
        resolved = true;

        resolveBasicLinks(model, adapter);

        linkListsScrappingContexts.addAll(mappedLinkedListsScrappingContexts.values());
    }


    /**
     * @see LinksFollower#getScrappingContexts()
     * @return see {@link LinksFollower#getScrappingContexts()}
     */
    @Override
    @NotNull
    public List<LinkScrappingContext> getScrappingContexts() {

        return scrappingContexts;
    }

    /**
     * @see  LinksFollower#getLinkListsScrappingContexts()
     * @return see {@link LinksFollower#getLinkListsScrappingContexts()}
     */
    @Override
    @NotNull
    public List<LinkListScrappingContext> getLinkListsScrappingContexts() {

        return linkListsScrappingContexts;
    }


    /**
     * <p>
     *     This is the core method of this implementation basically doing all the
     *     work to scan links through and prepare them. This method requires the
     *     two additional parameters because it is supposed to be used recursively
     *     for child pojos which will require a new {@link HtmlAdapter} with a new
     *     parent POJO instance.
     * </p>
     * <p>
     *     This method will basically just call {@link #resolveChildPojosLinks(Object, List)},
     *     {@link #resolveListLinks(Object, List)} and {@link #followLinks(List, Object, HtmlAdapter)}
     *     method to respect the three key points of the {@link LinksFollower} contract mentionned
     *     here {@link LinksFollower#resolveBasicLinks()}.
     * </p>
     * @param model the parent POJO instance to search links for.
     * @param adapter the {@link HtmlAdapter} for the model POJO.
     * @param <P> the POJO type of {@code model}.
     * @throws LinkException when one link could not be properly resolved and prepared.
     *                       This will only happend if {@code throwExceptions} was
     *                       enabled in any underlying {@link LinkScrappingContext}.
     *                       Otherwise, the exception will be logged within the furnished
     *                       {@link ExceptionLogger}.
     */
    private <P> void resolveBasicLinks(
            @NotNull final P model,
            @NotNull final HtmlAdapter<P> adapter
    ) throws LinkException
    {

        log.info("Resolving basic links for model type " + model.getClass());

        List<HtmlToPojoAnnotationMap<Link>> links = adapter.getFieldList(Link.class);
        List<HtmlToPojoAnnotationMap<HasLink>> hasLinks = adapter.getFieldList(HasLink.class);
        List<HtmlToPojoAnnotationMap<LinkListsFromBuilder>> linkListsFromBuilders = adapter.getFieldList(LinkListsFromBuilder.class);


        resolveChildPojosLinks(model, hasLinks);

        resolveListLinks(model, linkListsFromBuilders);

        followLinks(links, model, adapter);
    }


    /**
     * Resolve the {@link LinkListsFromBuilder} annotations of the current POJO.
     * @param model the model to scan {@link LinkListsFromBuilder} annotations for.
     * @param linkListsFromBuilders the List of {@link HtmlToPojoAnnotationMap} for
     *                              {@link LinkListsFromBuilder} currently retrieved.
     * @param <P> the Parent POJO type.
     * @param <U> the Child Pojo type of the list to create and populate.
     */
    @SuppressWarnings("unchecked")
    private <P, U> void resolveListLinks(P model, List<HtmlToPojoAnnotationMap<LinkListsFromBuilder>> linkListsFromBuilders) {

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

    /**
     * <p>
     *     This method will scan a POJO searching for {@link HasLink} annotation on top of a
     *     child POJO or list of child POJO typed field. If found, it will recursively call the
     *     {@link #resolveBasicLinks(Object, HtmlAdapter)} method for each of those POJOs.
     * </p>
     * <p>
     *     Depending on the field being a POJO typed field or a list of POJO typed field,
     *     it will either be directly pre-processed (POJO typed field) or pre-processed
     *     in a for loop for each single element of the original collection.
     * </p>
     * @param model the parent model POJO instance to scan for {@link HasLink} fields.
     * @param hasLinks the list of {@link HtmlToPojoAnnotationMap} containing {@link HasLink}
     *                 annotations already retrieved.
     * @param <P> the Parent POJO type.
     * @param <U> the Child Pojo type. This type will be inferred in the for loop and
     *            might therefore represent several different types during this method
     *            call.
     * @throws LinkException if any underlying {@link #resolveBasicLinks(Object, HtmlAdapter)}
     *                       call itself throws a {@link LinkException}.
     */
    @SuppressWarnings("unchecked")
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


    /**
     * <p>
     *     This method will follow direct links of a POJO annotated with {@link Link} for
     *     the String typed fields containing the URL to poll, and {@link LinkObject}
     *     or {@link LinkObjects} for the fields to populate with the future resulting
     *     scrap.
     * </p>
     * @param links the raw {@link HtmlToPojoAnnotationMap} for {@link Link} annotated fields
     *              to further analyse.
     * @param model the parent POJO instance to map resulting values to.
     * @param adapter the parent POJO class {@link HtmlAdapter}
     * @param <P> the Parent POJO type.
     * @param <U> the Child Pojo type. This type will be inferred in the for loop and
     *            might therefore represent several different types during this method
     *            call.
     * @throws LinkException if any of the {@link Link} annotated fields contains a
     *                       null value {@link NullLinkException}, isn't a String
     *                       typed field {@link LinkClassCastException} or if it cannot
     *                       be retrieved using reflection.
     */
    @SuppressWarnings("unchecked")
    private <P, U> void followLinks(
            @NotNull final List<HtmlToPojoAnnotationMap<Link>> links,
            @NotNull final U model,
            @NotNull final HtmlAdapter<U> adapter
    ) throws LinkException {

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


                LinkScrappingContext<P, U> lsc = buildContext(container, newFieldAdapter, requestEditor, editRequest);
                if(objField.getAnnotation(LinkObject.class) != null)
                        scrappingContexts.add(lsc);
                else
                {
                    LinkListScrappingContext<P, U> llsc =
                            mappedLinkedListsScrappingContexts
                                    .computeIfAbsent(
                                            objField,
                                            field -> new LinkListScrappingContext(field, model)
                                    );

                    llsc.add(lsc);
                }
            }

        }
    }

    /**
     * <p>
     *     This method will check if the {@link Link#regexCondition()} is met
     *     by the annotated field parsed and casted value.
     * </p>
     * @param link the {@link Link} annotated {@link HtmlToPojoAnnotationMap} field
     *             to analyse.
     * @param linkVal the retrieved field value to check regex against.
     * @return a boolean indicating if the {@link Link#regexCondition()} is met or not.
     */
    @Contract("_, null -> false")
    private boolean checkRegexCondition(HtmlToPojoAnnotationMap<Link> link, String linkVal){

        if(linkVal == null)
            return false;

        if(link.getAnnotation().regexCondition().equals(DEFAULT_REGEX_COND))
            return true;

        Pattern pattern = Pattern.compile(link.getAnnotation().regexCondition());

        return pattern.matcher(linkVal).find();
    }

    /**
     * <p>
     *     Gather the link value using reflection.
     * </p>
     * @param link the {@link Link} annotated {@link HtmlToPojoAnnotationMap} field
     *             to analyse.
     * @return the url retrieved from the corresponding {@link Link} annotated field.
     * @throws LinkException if any of the {@link Link} annotated fields contains a
     *                       null value {@link NullLinkException}, isn't a String
     *                       typed field {@link LinkClassCastException} or if it cannot
     *                       be retrieved using reflection.
     */
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


    /**
     * <p>
     *     This method will follow and prepare the {@link LinkListsFromBuilder}
     *     annotated fields.
     * </p>
     * @param links the list  of {@link LinkListsFromBuilder} annotated
     *              {@link HtmlToPojoAnnotationMap} fields to analyse.
     * @param parent the parent POJO instance to assign scrap results to.
     * @param adapter the {@link HtmlAdapter} of type {@code U}.
     * @param <P> the Parent POJO type.
     * @param <U> the child POJO type in the List to be created and assigned
     *            to {@code parent} POJO instance.
     * @return the resulting {@link LinkListScrappingContext} built.
     */
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


    /**
     * <p>
     *     This method will build a single {@link LinkScrappingContext} for
     *     a single link to scrap. It will be called under the hood by both
     *     {@link #followLinks(List, Object, HtmlAdapter)} and
     *     {@link #followLinkLists(HtmlToPojoAnnotationMap, Object, HtmlAdapter)}.
     * </p>
     * @param container the {@link LinkPreparatorHolder} to use to prepare and
     *                  build {@link LinkScrappingContext}.
     * @param adapter the child {@link HtmlAdapter} to use by the {@link HtmlAutoScrapper}
     *                to parse and scrap the {@code U} typed child to be scrapped element.
     * @param requestEditor the {@link HttpRequestEditor} instance to further prepare the
     *                      {@link BoundRequestBuilder} and {@code U} typed yet to come child
     *                      object.
     * @param editRequest wether the request should or shouldn't be edited.
     * @param <P> the parent POJO type.
     * @param <U> the child POJO type.
     * @return the built and prepared. and ready to use {@link LinkScrappingContext}.
     */
    @Nullable
    private <P, U> LinkScrappingContext<P, U> buildContext(
                            @NotNull  final LinkPreparatorHolder<P> container,
                            @NotNull  final HtmlAdapter<U> adapter,
                            @Nullable final HttpRequestEditor<P, U> requestEditor,
                                      final boolean editRequest
    ){

        if (!editRequest || (requestEditor != null && requestEditor.doRequest(container.getParent())))
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

    /**
     * <p>
     *     Inner method used by {@link #buildContext(LinkPreparatorHolder, HtmlAdapter, HttpRequestEditor, boolean)}
     *     to prepare the {@link BoundRequestBuilder} to use for the {@link LinkScrappingContext}.
     * </p>
     *
     * @param container the {@link LinkPreparatorHolder} to use to prepare and
     *                  build {@link LinkScrappingContext}.
     * @param requestEditor the {@link HttpRequestEditor} instance to further prepare the
     *                      {@link BoundRequestBuilder} and {@code U} typed yet to come child
     *                      object.
     * @param editRequest wether the request should or shouldn't be edited.
     * @param newObj the instanciated new child object. (With handled POJO injection).
     * @param <P> the parent POJO type.
     * @param <U> the child POJO type.
     * @return the new and prepared {@link BoundRequestBuilder} to be used for the
     *         scrapping operation of the {@link HtmlAutoScrapper}.
     */
    @NotNull
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

        if (editRequest && requestEditor != null)
            requestEditor.editRequest(req, container);
        return req;
    }


}
