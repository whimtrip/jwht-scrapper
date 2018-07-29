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
import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhthtmltopojo.exception.HtmlToPojoException;
import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlAdapter;
import fr.whimtrip.ext.jwhtscrapper.annotation.WarningSign;
import fr.whimtrip.ext.jwhtscrapper.exception.*;
import fr.whimtrip.ext.jwhtscrapper.intfr.BasicObjectMapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.HttpMetrics;
import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkListScrappingContext;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkScrappingContext;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by LOUISSTEIMBERG on 18/11/2017.
 */
public class HtmlAutoScrapper<T> {

    private static final Logger log = LoggerFactory.getLogger(HtmlAutoScrapper.class);

    private final HttpManagerClient httpManagerClient;
    private final BasicObjectMapper objectMapper;
    private final BoundRequestBuilderProcessor boundRequestBuilderProcessor;
    private final HtmlAdapter<T> htmlAdapter;
    private final HtmlToPojoEngine htmlToPojoEngine;
    private final ExceptionLogger exceptionLogger;
    private final Class<T> persistentClass;

    private final int warningSignDelay;
    private final boolean followRedirections;


    public HtmlAutoScrapper(
            HttpManagerClient httpManagerClient,
            HtmlToPojoEngine htmlToPojoEngine,
            BoundRequestBuilderProcessor boundRequestBuilderProcessor,
            BasicObjectMapper objectMapper,
            ExceptionLogger exceptionLogger,
            Class<T> clazz,
            boolean followRedirections,
            int warningSignDelay
    ) {
        this.httpManagerClient = httpManagerClient;
        this.boundRequestBuilderProcessor = boundRequestBuilderProcessor;
        this.objectMapper = objectMapper;
        this.exceptionLogger = exceptionLogger;

        this.persistentClass = clazz;

        htmlAdapter = htmlToPojoEngine.adapter(persistentClass);

        this.htmlToPojoEngine = htmlToPojoEngine;
        this.warningSignDelay = warningSignDelay;
        this.followRedirections = followRedirections;

    }

    public T scrap(@NotNull final BoundRequestBuilder req) throws ModelBindingException, LinkException, WarningSignException {
        return scrap(req, null);
    }

    public T scrap(@NotNull final BoundRequestBuilder req, @Nullable final T obj) throws ModelBindingException, LinkException, WarningSignException{
        return scrap(req,  obj, htmlAdapter, followRedirections);
    }

    public T scrapPost(@NotNull final String url, @Nullable final Map<String, Object> fields) throws ModelBindingException, LinkException, WarningSignException {
        return scrap(prepareScrapPost(url, fields));
    }

    public T scrapGet(@NotNull final String url) throws ModelBindingException, LinkException, WarningSignException {
        return scrap(prepareScrapGet(url));
    }

    public BoundRequestBuilder prepareScrapPost(@NotNull final String url)
    {
        return prepareScrapPost(url, null);
    }

    public BoundRequestBuilder prepareScrapPost(@NotNull final String url, @Nullable final Map<String, Object> fields)
    {
        BoundRequestBuilder req = httpManagerClient.preparePost(url);

        if(fields != null)
            for (Map.Entry<String, Object> field : fields.entrySet())
                req.addFormParam(field.getKey(), field.getValue().toString());


        return req;
    }

    public BoundRequestBuilder prepareScrapGet(@NotNull final String url)
    {
        return httpManagerClient.prepareGet(url);
    }

    public HttpMetrics getHttpMetrics() throws ScrapperUnsupportedException {
        return httpManagerClient.getHttpMetrics();
    }

    @SuppressWarnings("unchecked")
    private <U> U scrap(
            @NotNull        BoundRequestBuilder req,
            @Nullable       U obj,
            @NotNull  final HtmlAdapter<U> adapter,
                      final boolean followRedirections
    ) throws ModelBindingException, LinkException, WarningSignException
    {
        Class<U> mappedClazz =  obj == null ? (Class<U>) persistentClass :  (Class<U>) obj.getClass();
        String rawResponse =
                httpManagerClient.getResponse(req, followRedirections);

        try {

            obj = buildObject(obj, adapter, mappedClazz, rawResponse);
            resolveLinks(obj, adapter);

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
            req = boundRequestBuilderProcessor.recreateRequest(req, httpManagerClient);
            return scrap(req, obj, adapter, followRedirections);
        }
        catch (IOException | HtmlToPojoException e)
        {
            throw new ModelBindingException(e);
        }
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


    private <U> void resolveLinks(@NotNull final U obj, @NotNull final HtmlAdapter<U> adapter) throws LinkException, ModelBindingException{

        LinksFollower linksFollower = new LinksFollower(httpManagerClient, htmlToPojoEngine, exceptionLogger, obj, adapter);
        linksFollower.resolveBasicLinks();

        scrapAndSetLinkLists(linksFollower);

        scrapAndSetBasicLinks(linksFollower);
    }

    private void scrapAndSetBasicLinks(@NotNull final LinksFollower linksFollower) throws ModelBindingException, LinkException {

        for(LinkScrappingContext lsc : linksFollower.getScrappingContexts()) {

            Object newObj = null;
            try {
                newObj = scrap(lsc);
            }
            catch (ScrapperException e)
            {
                handleScrapperException(lsc, e);
            }

            if(newObj != null)
            {

                try {
                    WhimtripUtils.setObjectToField(lsc.getFieldToBeSet(), lsc.getParentObject(), newObj);
                }

                catch (IllegalAccessException e) {
                    exceptionLogger.logException(e);
                    throw new ModelBindingException(e);
                }
            }
        }
    }

    private void handleScrapperException(LinkScrappingContext lsc, ScrapperException e) throws LinkException {

        if(lsc.throwExceptions())
            throw new LinkException(e);

        exceptionLogger.logException(e);
    }

    private void scrapAndSetLinkLists(@NotNull final LinksFollower linksFollower) throws ModelBindingException, LinkException {

        for(LinkListScrappingContext llsc : linksFollower.getLinkListsScrappingContexts()) {
            List ulist = buildLinkListScraps(llsc);

            try {
                WhimtripUtils.setObjectToField(llsc.getFieldToBeSet(), llsc.getParentObject(), ulist);
            }

            catch (IllegalAccessException e) {
                exceptionLogger.logException(e);
                throw new ModelBindingException(e);
            }
        }
    }

    private <U> List<U> buildLinkListScraps(LinkListScrappingContext<?, U> llsc) throws LinkException {

        List<U> uList = new ArrayList<>();
        for(LinkScrappingContext<?, U> lsc : llsc) {

            U newObj = null;
            try{
                newObj = scrap(lsc);
            }
            catch (ScrapperException e) {
                handleScrapperException(lsc, e);
            }
            if(newObj != null)
                uList.add(newObj);
        }
        return uList;
    }

    private <U> U scrap(LinkScrappingContext<?, U> lsc) throws LinkException {

        return scrap(lsc.getBoundRequestBuilder(), lsc.getNewObj(), lsc.getAdapter(), lsc.followRedirections());
    }


}
