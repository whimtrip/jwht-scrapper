/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.core.util.WhimtripUtils;
import fr.whimtrip.core.util.exception.ObjectCreationException;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhthtmltopojo.adapter.HtmlToPojoAnnotationMap;
import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlAdapter;
import fr.whimtrip.ext.jwhtscrapper.annotation.HasLink;
import fr.whimtrip.ext.jwhtscrapper.annotation.Link;
import fr.whimtrip.ext.jwhtscrapper.annotation.LinkListsFromBuilder;
import fr.whimtrip.ext.jwhtscrapper.annotation.WarningSign;
import fr.whimtrip.ext.jwhtscrapper.exception.LinkException;
import fr.whimtrip.ext.jwhtscrapper.exception.ModelBindingException;
import fr.whimtrip.ext.jwhtscrapper.exception.WarningSignException;
import fr.whimtrip.ext.jwhtscrapper.intfr.BasicObjectMapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.HttpRequestEditor;
import fr.whimtrip.ext.jwhtscrapper.intfr.LinkListFactory;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

/**
 * Created by LOUISSTEIMBERG on 18/11/2017.
 */
public class HtmlAutoScrapper<T> {

    private static final Logger log = LoggerFactory.getLogger(HtmlAutoScrapper.class);

    private final ProxyManagerClient proxyManagerClient;
    private final BasicObjectMapper objectMapper;
    private final BoundRequestBuilderProcessor boundRequestBuilderProcessor;
    private final HtmlAdapter<T> htmlAdapter;
    private final HtmlToPojoEngine htmlToPojoEngine;
    private final Class<T> persistentClass;




    private final boolean throwExceptions;

    private final int warningSignDelay;
    private final boolean parallelizeLinkListPolling;
    private final boolean followRedirections;


    public HtmlAutoScrapper(
            ProxyManagerClient proxyManagerClient,
            HtmlToPojoEngine htmlToPojoEngine,
            BoundRequestBuilderProcessor boundRequestBuilderProcessor,
            BasicObjectMapper objectMapper,
            Class<T> clazz,
            boolean throwEx,
            boolean parallelizeLinkListPolling,
            boolean followRedirections,
            int warningSignDelay
    ) {
        this.proxyManagerClient = proxyManagerClient;
        this.boundRequestBuilderProcessor = boundRequestBuilderProcessor;
        this.objectMapper = objectMapper;
        this.parallelizeLinkListPolling = parallelizeLinkListPolling;

        this.persistentClass = clazz;

        htmlAdapter = htmlToPojoEngine.adapter(persistentClass);

        this.htmlToPojoEngine = htmlToPojoEngine;
        throwExceptions = throwEx;
        this.warningSignDelay = warningSignDelay;
        this.followRedirections = followRedirections;

    }

    public T scrap(BoundRequestBuilder req) throws ExecutionException, InterruptedException, ModelBindingException {
        return scrap(req, null);
    }

    public T scrap(BoundRequestBuilder req, T obj) throws ExecutionException, InterruptedException, ModelBindingException{
        return scrap(req,  obj, htmlAdapter);
    }

    private <U> U scrap(BoundRequestBuilder req, U obj, HtmlAdapter<U> adapter)
            throws ExecutionException, InterruptedException, ModelBindingException
    {
        return scrap(req, obj, adapter, followRedirections);
    }

    @SuppressWarnings("unchecked")
    private <U> U scrap(BoundRequestBuilder req, U obj, HtmlAdapter<U> adapter, boolean followRedirections)
            throws ExecutionException, InterruptedException, ModelBindingException
    {
        return scrap(req, obj, adapter, followRedirections, obj == null ? (Class<U>) persistentClass :  (Class<U>) obj.getClass());
    }

    private <U> U scrap(BoundRequestBuilder req, U obj, HtmlAdapter<U> adapter, boolean followRedirections, Class<U> mappedClazz)
            throws ExecutionException, InterruptedException, ModelBindingException
    {
        String rawResponse =
                proxyManagerClient.getResponse(req, followRedirections);

        try {

            obj = buildObject(obj, adapter, mappedClazz, rawResponse);
            resolveBasicLinks(obj, adapter);

            return obj;

        }
        catch(WarningSignException e)
        {
            log.info("A warning sign was triggered! " + e.getMessage());

            if(e.getAction() == WarningSign.Action.THROW_EXCEPTION)
            {
                log.info("This warning sign means actual scrapped handled a fatal error which" +
                        " shouldn't lead to further scrapping for that object");
                throw e;
            }

            if(e.getAction() == WarningSign.Action.STOP_ACTUAL_SCRAP)
            {
                log.info("This warning sign means this object shouldn't be further scrapped");
                return obj;
            }

            boundRequestBuilderProcessor.printReq(req);
            WhimtripUtils.waitForWithOutputToConsole((long) warningSignDelay, 20);
            req = boundRequestBuilderProcessor.recreateRequest(req, proxyManagerClient);
            return scrap(req, obj, adapter, followRedirections, mappedClazz);
        }
        catch (IOException e) {
            throw new ModelBindingException(e);
        }
    }

    public T scrapPost(String url) throws ExecutionException, InterruptedException {
        return (T) scrapPost(url, new HashMap());
    }

    public T scrapPost(String url, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        return scrap(prepareScrapPost(url, fields));
    }

    public T scrapGet(String url) throws ExecutionException, InterruptedException {
        return scrap(prepareScrapGet(url));
    }

    public BoundRequestBuilder prepareScrapPost(String url)
    {
        return prepareScrapPost(url, new HashMap());
    }

    public BoundRequestBuilder prepareScrapPost(String url, Map<String, Object> fields)
    {
        BoundRequestBuilder req = proxyManagerClient.post(url);
        for(Map.Entry<String, Object> field : fields.entrySet())
        {
            req.addFormParam(field.getKey(), field.getValue().toString());
        }
        return req;
    }

    public BoundRequestBuilder prepareScrapGet(String url)
    {
        return proxyManagerClient.get(url);
    }


    private <U> U buildObject(
            @Nullable U obj,
            @Nullable final HtmlAdapter<U> adapter,
            @Nullable final Class<U> mappedClazz,
            @NotNull final String rawResponse
    ) throws IOException
    {
        if (obj == null) {
            obj = objectMapper == null ?
                    adapter.fromHtml(rawResponse)
                    : objectMapper.readValue(rawResponse, mappedClazz);
        } else {
            obj = objectMapper == null ?
                    adapter.fromHtml(rawResponse, obj)
                    : objectMapper.readValue(rawResponse, mappedClazz, obj);
        }
        return obj;
    }

    private <P, U> void resolveBasicLinks(P model, HtmlAdapter<P> adapter) throws ExecutionException, InterruptedException {

        log.info("Resolving basic links for model type " + model.getClass());

        List<HtmlToPojoAnnotationMap<Link>> links = adapter.getFieldList(Link.class);
        List<HtmlToPojoAnnotationMap<HasLink>> hasLinks = adapter.getFieldList(HasLink.class);
        List<HtmlToPojoAnnotationMap<LinkListsFromBuilder>> linkListsFromBuilders = adapter.getFieldList(LinkListsFromBuilder.class);


        for(HtmlToPojoAnnotationMap<HasLink> hasLink : hasLinks)
        {
            if(Collection.class.isAssignableFrom((Class<U>) hasLink.getField().getType()))
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

        for(HtmlToPojoAnnotationMap<LinkListsFromBuilder> linkList : linkListsFromBuilders)
        {
            if(List.class.isAssignableFrom(linkList.getField().getType())) {

                List elements  =  new ArrayList();

                Type genericType = linkList.getField().getGenericType();
                Type type = ((ParameterizedType) genericType).getActualTypeArguments()[0];

                List<U> ulist = followLinkLists(linkList, model, htmlToPojoEngine.adapter((Class<U>) type));

                try {
                    WhimtripUtils.setObjectToField(linkList.getField(), model, ulist);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            else{
                throw new ObjectCreationException(
                        LinkListsFromBuilder.class.getName() + " cannot be used on a non "
                        + List.class.getName() + " field.");
            }
        }

        followLinks(links, model, adapter);
    }

    public <U, P> void followLinks(List<HtmlToPojoAnnotationMap<Link>> links, P model, HtmlAdapter<P> adapter) throws ExecutionException, InterruptedException {

        for (HtmlToPojoAnnotationMap<Link> link : links) {
            Field objField = ((ScrapperHtmlAdapter<P>) adapter).getLinkObject(link);

            if (objField == null) throw new LinkException(link.getField());

            Pattern pattern = Pattern.compile(link.getAnnotation().regexCondition());
            String linkVal = "";
            try{
                linkVal = WhimtripUtils.getObjectFromField(link.getField(), model);
            }catch(IllegalAccessException e)
            {
                throw new LinkException(e);
            }

            if(pattern.matcher(linkVal).find())
            {

                HtmlAdapter<U> newFieldAdapter = (HtmlAdapter<U>) htmlToPojoEngine.adapter(objField.getType());

                boolean editRequest = link.getAnnotation().editRequest();

                String url;

                try {

                    url = WhimtripUtils.getObjectFromField(link.getField(), model);

                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                    throw new LinkException(e);
                }

                LinkPreparatorHolder container =
                        new LinkPreparatorHolder()
                        .buildFields(link.getAnnotation().fields())
                        .setFollowRedirections(link.getAnnotation().followRedirections())
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

                U newObj = (U) pollLink(container, newFieldAdapter, editRequest, requestEditor);

                if(newObj != null)
                {
                    try {
                        WhimtripUtils.setObjectToField(objField, model, newObj);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        throw new LinkException(e);
                    }
                }
            }

        }
    }

    public <U, P> List<U> followLinkLists(HtmlToPojoAnnotationMap<LinkListsFromBuilder> links,
                                       P parent, HtmlAdapter<U> adapter) throws ExecutionException, InterruptedException {
        LinkListFactory<P> listFactory = WhimtripUtils.createNewInstance(links.getAnnotation().value());
        List<U> ulist = new ArrayList<>();

        List<LinkPreparatorHolder> containers = listFactory.createLinkPreparatorLists(parent, links.getField());

        List<FutureTask<U>> ftus = new ArrayList<>();


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

            if(parallelizeLinkListPolling) {
                FutureTask<U> ftu = new FutureTask<U>(new LinkListElementCallable(
                        cntn, adapter, editRequest, requestEditor
                ));

                ftus.add(ftu);
            }
            else{
                U newObj = (U) pollLink(cntn, adapter, editRequest, requestEditor);

                if (newObj != null)
                    ulist.add(newObj);
            }



        }

        if(parallelizeLinkListPolling) {

            for (FutureTask<U> ft : ftus) {
                Thread t = new Thread(ft);
                t.start();
            }

            for (FutureTask<U> ft : ftus) {


                U newObj = null;
                try {
                    newObj = ft.get();
                } catch (InterruptedException | ExecutionException e) {
                    if (throwExceptions) {
                        e.printStackTrace();
                        throw e;
                    }
                }

                if (newObj != null)
                    ulist.add(newObj);
            }
        }

        return ulist;

    }


    public <U, P> U pollLink(LinkPreparatorHolder<P> container, HtmlAdapter<U> adapter,
                             boolean editRequest, HttpRequestEditor<P, U> requestEditor )
            throws ExecutionException, InterruptedException {



        if (!editRequest || requestEditor.doRequest(container.getParent()))
        {
            U newObj = adapter.createNewInstance(container.getParent());

            if (editRequest)
                requestEditor.prepareObject(newObj, container.getParent(), container);



            BoundRequestBuilder req;

            if (container.getMethod() == Link.Method.GET)
            {
                req = proxyManagerClient.get(container.getUrl());
            }
            else
            {
                req = proxyManagerClient.post(container.getUrl());

                for(Map.Entry<String, Object> fieldEntr: container.getFields().entrySet())
                {
                    req.addFormParam(fieldEntr.getKey(), fieldEntr.getValue().toString());
                }
            }

            if (editRequest)
                requestEditor.editRequest(req, container);


            scrap(req, newObj, adapter, container.isFollowRedirections());

            return newObj;

        }

        return null;
    }



    public ProxyManagerClient getProxyManagerClient() {
        return proxyManagerClient;
    }

    private class LinkListElementCallable<U, P> implements Callable<U>
    {
        private final LinkPreparatorHolder<P> container;
        private final HtmlAdapter<U> adapter;
        private final boolean editRequest;
        private final  HttpRequestEditor<P, U> requestEditor;

        public LinkListElementCallable(
                LinkPreparatorHolder<P> container,
                HtmlAdapter<U> adapter,
                boolean editRequest,
                HttpRequestEditor<P, U> requestEditor
        ) {
            this.container = container;
            this.adapter = adapter;
            this.editRequest = editRequest;
            this.requestEditor = requestEditor;
        }

        @Override
        public U call() throws Exception {
            return pollLink(container, adapter, editRequest, requestEditor);
        }
    }

}
