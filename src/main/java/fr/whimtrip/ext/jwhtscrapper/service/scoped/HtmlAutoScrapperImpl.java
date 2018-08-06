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
import fr.whimtrip.ext.jwhtscrapper.impl.ScrapperHtmlAdapterFactory;
import fr.whimtrip.ext.jwhtscrapper.intfr.BasicObjectMapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.HtmlAutoScrapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.HttpMetrics;
import fr.whimtrip.ext.jwhtscrapper.intfr.LinksFollower;
import fr.whimtrip.ext.jwhtscrapper.service.base.BoundRequestBuilderProcessor;
import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkListScrappingContext;
import fr.whimtrip.ext.jwhtscrapper.service.holder.LinkScrappingContext;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 30/07/18</p>
 *
 * <p>
 *     Default implementation of {@link HtmlAutoScrapper}. As stated in the interface
 *     javadoc, we implemented it will all given recommendations including :
 * </p>
 *
 * <ul>
 *     <li>
 *         {@link HttpWithProxyManagerClient}
 *     </li>
 *     <li>
 *         {@link LinksFollowerImpl}
 *     </li>
 *     <li>
 *         {@link HtmlToPojoEngine} with {@link ScrapperHtmlAdapterFactory}.
 *     </li>
 *     <li>
 *         {@link BasicObjectMapper} is accepted.
 *     </li>
 * </ul>
 *
 * @see HtmlAutoScrapper
 * @author Louis-wht
 * @since 1.0.0
 */
public class HtmlAutoScrapperImpl<T> implements HtmlAutoScrapper<T> {

    private static final Logger log = LoggerFactory.getLogger(HtmlAutoScrapperImpl.class);

    private final HttpManagerClient httpManagerClient;
    private final BasicObjectMapper objectMapper;
    private final BoundRequestBuilderProcessor boundRequestBuilderProcessor;
    private final HtmlAdapter<T> htmlAdapter;
    private final HtmlToPojoEngine htmlToPojoEngine;
    private final ExceptionLogger exceptionLogger;
    private final Class<T> persistentClass;

    private final int warningSignDelay;
    private final boolean followRedirections;


    /**
     * <p>Default Constructor</p>
     *
     * @param exceptionLogger the exception logger that will be used by both the
     *                        {@link HttpWithProxyManagerClient} and the {@link HtmlAutoScrapper}
     * @param htmlToPojoEngine the core html to pojo engine allowing us to parse
     *                         HTML input to java POJOs.
     * @param objectMapper the object mapper to use for mapping differently formatted
     *                     strings.
     * @param boundRequestBuilderProcessor the request processor used for headers,
     *                                    cookies etc modfications as well as other
     *                                    eventual use cases.
     * @param httpManagerClient the {@link HttpWithProxyManagerClient} that will be used under the
     *               hood by the {@link HtmlAutoScrapper}.<br>
     *
     * @param clazz the class to map resulting outputs to.<br>
     *
     *
     * @param followRedirections wether HTTP redirections should be followed
     *                           or not (HTTP redirections is valid if status
     *                           code is {@code 301} or {@code 302} and when
     *                           the {@code Location} header is not empty.<br>
     *
     * @param warningSignDelay delay before retrying any action in the case
     *                         a {@link WarningSign} was triggered and only if it
     *                         was set to {@link WarningSign.Action#RETRY}.<br>
     */
    public HtmlAutoScrapperImpl(
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

    /**
     * @see HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)
     * @param req see {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}
     * @param obj see {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}
     * @return see {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}
     * @throws ModelBindingException see {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}
     * @throws LinkException see {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}
     * @throws WarningSignException see {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}
     */
    @NotNull
    @Override
    public T scrap(@NotNull final BoundRequestBuilder req, @Nullable final T obj)
            throws ModelBindingException, LinkException, WarningSignException
    {
        return scrap(req,  obj, htmlAdapter, followRedirections);
    }


    /**
     * @see HtmlAutoScrapper#scrapPost(String, Map)
     * @param url see {@link HtmlAutoScrapper#scrapPost(String, Map)}
     * @param fields see {@link HtmlAutoScrapper#scrapPost(String, Map)}
     * @return see {@link HtmlAutoScrapper#scrapPost(String, Map)}
     * @throws ModelBindingException see {@link HtmlAutoScrapper#scrapPost(String, Map)}
     * @throws LinkException see {@link HtmlAutoScrapper#scrapPost(String, Map)}
     * @throws WarningSignException see {@link HtmlAutoScrapper#scrapPost(String, Map)}
     */
    @NotNull
    @Override
    public T scrapPost(@NotNull final String url, @Nullable final Map<String, Object> fields)
            throws ModelBindingException, LinkException, WarningSignException
    {
        return scrap(prepareScrapPost(url, fields));
    }


    /**
     * @see HtmlAutoScrapper#scrapGet(String)
     * @param url see {@link HtmlAutoScrapper#scrapGet(String)}
     * @return see {@link HtmlAutoScrapper#scrapGet(String)}
     * @throws ModelBindingException see {@link HtmlAutoScrapper#scrapGet(String)}
     * @throws LinkException see {@link HtmlAutoScrapper#scrapGet(String)}
     * @throws WarningSignException see {@link HtmlAutoScrapper#scrapGet(String)}
     */
    @NotNull
    @Override
    public T scrapGet(@NotNull final String url)
            throws ModelBindingException, LinkException, WarningSignException
    {
        return scrap(prepareScrapGet(url));
    }

    /**
     * @see HtmlAutoScrapper#prepareScrapPost(String, Map) 
     * @param url see {@link HtmlAutoScrapper#prepareScrapPost(String, Map)}
     * @param fields see {@link HtmlAutoScrapper#prepareScrapPost(String, Map)}
     * @return see {@link HtmlAutoScrapper#prepareScrapPost(String, Map)}
     */
    @NotNull
    @Override
    public BoundRequestBuilder prepareScrapPost(@NotNull final String url, @Nullable final Map<String, Object> fields)
    {
        BoundRequestBuilder req = httpManagerClient.preparePost(url);

        if(fields != null)
            for (Map.Entry<String, Object> field : fields.entrySet())
                req.addFormParam(field.getKey(), field.getValue().toString());


        return req;
    }

    /**
     * @see HtmlAutoScrapper#prepareScrapGet(String)
     * @param url see {@link HtmlAutoScrapper#prepareScrapGet(String)}
     * @return see {@link HtmlAutoScrapper#prepareScrapGet(String)}
     */
    @NotNull
    @Override
    public BoundRequestBuilder prepareScrapGet(@NotNull final String url)
    {
        return httpManagerClient.prepareGet(url);
    }

    /**
     * @see HtmlAutoScrapper#getHttpMetrics()
     * @return {@link HtmlAutoScrapper#getHttpMetrics()}
     * @throws ScrapperUnsupportedException see {@link HtmlAutoScrapper#getHttpMetrics()}
     */
    @NotNull
    @Override
    public HttpMetrics getHttpMetrics() throws ScrapperUnsupportedException {
        return httpManagerClient.getHttpMetrics();
    }

    /**
     * <p>
     *     This provide the core private method that will perform scrapping
     *     related tasks. It conforms to all recommandations and contracts
     *     stipulated in {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}.
     * </p>
     * @see HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)
     * @param req the prepared {@link BoundRequestBuilder}
     * @param obj the object to map the resulting scrap to.
     * @param adapter the {@link HtmlAdapter} to use to map the resulting
     *                HTML body to a POJO. if the {@link BasicObjectMapper}
     *                is used instead, the adapter will still be used to perform
     *                field injection, links following and warning sign triggering.
     * @param followRedirections wether HTTP redirections should be followed or not.
     * @param <U> the type of the POJO to map it to. This inner method can be called
     *            recursively for links scrapping with other POJOs type. this explain
     *           why {@code T} is not used here.
     * @return the scrapped and ready to use POJO instance.
     * @throws ModelBindingException see {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}.
     * @throws LinkException see {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}.
     * @throws WarningSignException see {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}.
     */
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
            return scrap(req, obj, adapter, followRedirections);
        }
        catch (IOException | HtmlToPojoException e)
        {
            throw new ModelBindingException(e);
        }
    }


    /**
     * <p>
     *     Will call {@link #scrap(BoundRequestBuilder, Object, HtmlAdapter, boolean)}
     *     with all context parameters given by an {@link LinkScrappingContext}.
     * </p>
     * @param lsc the {@link LinkScrappingContext} to use to perform the scrap operation.
     * @param <U> the type of POJO instance it should return.
     * @return the corresponding {@code U} pojo instance.
     * @throws ModelBindingException see {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}.
     * @throws LinkException see {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}.
     * @throws WarningSignException see {@link HtmlAutoScrapper#scrap(BoundRequestBuilder, Object)}.
     */
    private <U> U scrap(LinkScrappingContext<?, U> lsc)
            throws ModelBindingException, LinkException, WarningSignException
    {
        return scrap(lsc.getBoundRequestBuilder(), lsc.getNewObj(), lsc.getAdapter(), lsc.followRedirections());
    }


    /**
     * <p>
     *     This method will build String body to a POJO using either
     *     {@link HtmlAdapter} to map HTML or {@link BasicObjectMapper}
     *     to map any other input format to POJO with any other rules it
     *     might imply.
     * </p>
     * @param obj the object to build the scrapping result to. Might be null.
     * @param adapter the adapter to use. Might be null if {@link BasicObjectMapper}
     *                is provided instead.
     * @param mappedClazz the clazz to map the body to.
     * @param rawResponse the raw String body response from the HTTP scrapping request.
     * @param <U> the type of POJO to map it to.
     * @return fully built and scrapped {@code U} POJO instance
     * @throws IOException if Object binding didn't work as expected with {@link BasicObjectMapper}.
     * @throws HtmlToPojoException if Object binding failed with {@link HtmlAdapter}.
     */
    private <U> U buildObject(
            @Nullable U obj,
            @Nullable final HtmlAdapter<U> adapter,
            @Nullable final Class<U> mappedClazz,
            @NotNull final String rawResponse
    ) throws IOException, HtmlToPojoException
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


    /**
     * <p>
     *     This method will handle all link following tasks related.
     *     This includes three main steps:
     * </p>
     * <ul>
     *     <li>
     *         Resolving all links to be searched and polled using {@link LinksFollowerImpl}
     *         default implementation of {@link LinksFollower}.
     *     </li>
     *     <li>
     *         Scrap those links using {@link #scrap(LinkScrappingContext)} method.
     *     </li>
     *     <li>
     *         Set the resulting the values to the field.
     *     </li>
     * </ul>
     *
     * <p>
     *     Additionally Link lists and single links are handled separately because
     *     list of links requires to first instanciate a list to append every link
     *     entry scrap result to.
     * </p>
     * @param obj the object to resolve links for.
     * @param adapter the {@link HtmlAdapter} to use to analyse the links to further scrap.
     * @param <U> the type of the POJO instance to scrap links for.
     * @throws LinkException when thrown by underlying {@link LinksFollower#resolveBasicLinks()}
     *                       or if a scrapping exception when scrapping the links in which case
     *                       the original exception can be retrieved using {@link Throwable#getCause()}.
     * @throws ModelBindingException if any field setting operation failed due to POJO reflection
     *                               access failure in which case the execption should be corrected
     *                               before starting the scrapping once again.
     */
    private <U> void resolveLinks(@NotNull final U obj, @NotNull final HtmlAdapter<U> adapter) throws LinkException, ModelBindingException{

        LinksFollower linksFollower = new LinksFollowerImpl(httpManagerClient, htmlToPojoEngine, exceptionLogger, obj, adapter);
        linksFollower.resolveBasicLinks();

        scrapAndSetLinkLists(linksFollower);

        scrapAndSetBasicLinks(linksFollower);
    }

    /**
     * <p>
     *     This method will start a simple Link scrap and set the value to the
     *     corresponding field.
     * </p>
     * @param linksFollower the {@link LinksFollower} instance holding resolved links to
     *                      scrap.
     * @throws LinkException when thrown by underlying {@link LinksFollower#resolveBasicLinks()}
     *                       or if a scrapping exception when scrapping the links in which case
     *                       the original exception can be retrieved using {@link Throwable#getCause()}.
     * @throws ModelBindingException if any field setting operation failed due to POJO reflection
     *                               access failure in which case the execption should be corrected
     *                               before starting the scrapping once again.
     */
    private void scrapAndSetBasicLinks(@NotNull final LinksFollower linksFollower) throws ModelBindingException, LinkException {

        for(LinkScrappingContext lsc : linksFollower.getScrappingContexts()) {

            Object newObj = null;
            try {
                newObj = scrap(lsc);
            }
            catch (ScrapperException e)
            {
                handleScrapperException(lsc.throwExceptions(), e);
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

    /**
     * <p>Will handle Scrapper Exception and turn them into {@link ScrapperException}</p>
     * @param throwExceptions wether the exception should be thrown or not.
     * @param e the underlying {@link ScrapperException}
     * @throws LinkException if {@code throwExceptions} is set to true.
     */
    @Contract("true, _ -> fail")
    private void handleScrapperException(boolean throwExceptions, ScrapperException e) throws LinkException {

        if(throwExceptions)
            throw new LinkException(e);

        exceptionLogger.logException(e);
    }

    /**
     * <p>
     *     This method will start a link list scrap and set the resulting list
     *     value to the corresponding field.
     * </p>
     * @param linksFollower the {@link LinksFollower} instance holding resolved links to
     *                      scrap.
     * @throws LinkException when thrown by underlying {@link LinksFollower#resolveBasicLinks()}
     *                       or if a scrapping exception when scrapping the links in which case
     *                       the original exception can be retrieved using {@link Throwable#getCause()}.
     * @throws ModelBindingException if any field setting operation failed due to POJO reflection
     *                               access failure in which case the execption should be corrected
     *                               before starting the scrapping once again.
     */
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

    /**
     * <p>
     *     The Link List to POJO List builder method. For each {@link LinkListScrappingContext}
     *     of the origin list, it will simply call {@link #scrap(LinkScrappingContext)}
     *     and then add the resulting value if not null to the new list being created.
     * </p>
     * @param llsc the {@link LinkListScrappingContext} to scrap all links for.
     * @param <U> the type of POJO instances in the list to return.
     * @return a list of {@code U} typed instances freshly scrapped from the
     *         {@link LinkScrappingContext} contained in {@code llsc}.
     * @throws LinkException when a scrapping operation failed if {@link LinkScrappingContext#throwExceptions()}
     *                       returned true.
     */
    private <U> List<U> buildLinkListScraps(LinkListScrappingContext<?, U> llsc) throws LinkException {

        List<U> uList = new ArrayList<>();
        for(LinkScrappingContext<?, U> lsc : llsc) {

            U newObj = null;
            try{
                newObj = scrap(lsc);
            }
            catch (ScrapperException e) {
                handleScrapperException(lsc.throwExceptions(), e);
            }
            if(newObj != null)
                uList.add(newObj);
        }
        return uList;
    }



}
