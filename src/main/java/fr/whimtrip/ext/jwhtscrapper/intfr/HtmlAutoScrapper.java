package fr.whimtrip.ext.jwhtscrapper.intfr;

import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhthtmltopojo.exception.HtmlToPojoException;
import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlAdapter;
import fr.whimtrip.ext.jwhtscrapper.annotation.WarningSign;
import fr.whimtrip.ext.jwhtscrapper.exception.LinkException;
import fr.whimtrip.ext.jwhtscrapper.exception.ModelBindingException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperUnsupportedException;
import fr.whimtrip.ext.jwhtscrapper.exception.WarningSignException;
import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.holder.HttpManagerConfig;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.ScrapperHtmlAdapter;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 30/07/18</p>
 *
 * <p>
 *     An HtmlAutoScrapper is the core processing unit of this library, providing
 *     a gateway to scrap manually if communicating directly with the implementing
 *     class or dynamically if using it through an {@link AutomaticScrapperClient}.
 * </p>
 *
 * <p>
 *     Yet, only GET and POST methods are supported. Underlying implemetation should
 *     be able to accept all input requirements that a {@link HttpManagerClient}
 *     must support (see {@link HttpManagerConfig}). To this extent, using an implementation
 *     of {@link HttpManagerClient} within the processing unit implementing this
 *     interface is recommended.
 *
 *     The imlementation should also accept all input requirements followed by
 *     {@link LinksFollower} implementations. It is also advised to use a
 *     dedicated {@link LinksFollower} implementation.
 *
 *     It should also be able to deal with the {@link WarningSign} policy.
 *
 *     Finally, it should accept any custom {@link BasicObjectMapper} to map
 *     HTTP bodies to POJOs. If input is in HTML format, it is recommended to
 *     use a {@link ScrapperHtmlAdapter}.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface HtmlAutoScrapper<T> {


    /**
     * <p>
     *     This method should delegate it's processing to {@link #scrap(BoundRequestBuilder, Object)}
     * </p>
     * @param req the built {@link BoundRequestBuilder} to poll and scrap.
     * @return the scrapped {@code T} typed instance.
     * @throws ModelBindingException see {@link #scrap(BoundRequestBuilder, Object)}
     * @throws LinkException see {@link #scrap(BoundRequestBuilder, Object)}
     * @throws WarningSignException see {@link #scrap(BoundRequestBuilder, Object)}
     */
    @NotNull
    default T scrap(@NotNull final BoundRequestBuilder req)
            throws ModelBindingException, LinkException, WarningSignException
    {
        return scrap(req, null);
    }

    /**
     * <p>
     *     As stated above (see {@link HtmlAutoScrapper}, a scrapping operation should
     *     accept all of the contracts above mentionned.
     * </p>
     * <p>
     *     Usually, a scrapping operation should be performed this way :
     * </p>
     * <ul>
     *     <li>Do the HTTP request following contracts detailed by {@link HttpManagerClient}</li>
     *     <li>
     *         Map the String HTML body of the response to a POJO using default provided
     *         {@link HtmlToPojoEngine} or the input {@link BasicObjectMapper}. In any case,
     *         you should use {@link HtmlToPojoEngine} and {@link HtmlAdapter} to provide
     *         field injection support, link annotated fields and {@link WarningSign}
     *         annotated fields parsing. Default implementation of this class for examle
     *         only uses {@link HtmlAdapter} to provide those functionnalities when
     *         a custom {@link BasicObjectMapper} has been provided.
     *     </li>
     *     <li>
     *         Catch any {@link WarningSignException} and handle it accordingly.
     *     </li>
     *     <li>
     *         Follow and scrap further Links. {@link LinksFollower}
     *     </li>
     * </ul>
     * @param req the prepared {@link BoundRequestBuilder} that will be used to perform
     *            the  HTTP request.
     * @param obj The object map the resulting scrap to.
     * @return the scrapped resulting object.
     * @throws ModelBindingException If any binding operation failed. It might be due to an
     *                               IOException during either the HTTP request or within the
     *                               {@link BasicObjectMapper} scope if used. It could also be
     *                               due to any {@link HtmlToPojoException} that will further
     *                               be catched and thrown as a {@link ModelBindingException}.
     *                               Please note that this exception always has a
     *                               {@link Throwable#getCause()} with the real exception cause
     *                               which might in some cases itself have its own inner cause
     *                               stacktracke.<br>
     * @throws LinkException If any Link following exception occures. It might be due to link with
     *                       null value. badly typed / annotated POJO...<br>
     * @throws WarningSignException When a catched {@link WarningSignException} requires to be
     *                              thrown given the input conditions of the implementing class.
     */
    @NotNull
    T scrap(@NotNull final BoundRequestBuilder req, @Nullable final T obj)
            throws ModelBindingException, LinkException, WarningSignException;

    /**
     * <p>
     *     Will directly {@link #prepareScrapPost(String, Map)} a request and
     *     then {@link #scrap(BoundRequestBuilder)} it.
     * </p>
     * @param url the url to scrap at.
     * @param fields the fields to add to the post request.
     * @return scrapped T typed POJO instance.
     * @throws ModelBindingException see {@link #scrap(BoundRequestBuilder, Object)}
     * @throws LinkException see {@link #scrap(BoundRequestBuilder, Object)}
     * @throws WarningSignException see {@link #scrap(BoundRequestBuilder, Object)}
     */
    @NotNull
    T scrapPost(@NotNull final String url, @Nullable final Map<String, Object> fields)
            throws ModelBindingException, LinkException, WarningSignException;


    /**
     * <p>
     *     Will directly {@link #prepareScrapGet(String)} a request and
     *     then {@link #scrap(BoundRequestBuilder)} it.
     * </p>
     * @param url the url to scrap at.
     * @return scrapped T typed POJO instance.
     * @throws ModelBindingException see {@link #scrap(BoundRequestBuilder, Object)}
     * @throws LinkException see {@link #scrap(BoundRequestBuilder, Object)}
     * @throws WarningSignException see {@link #scrap(BoundRequestBuilder, Object)}
     */
    @NotNull
    T scrapGet(@NotNull final String url)
            throws ModelBindingException, LinkException, WarningSignException;


    /**
     * <p>
     *     Simplified call to {@link #prepareScrapPost(String, Map)} that will send
     *      a {@link Map#of()} empty map.
     * </p>
     * @param url the url to scrap at.
     * @return the built and prepared {@link BoundRequestBuilder}. It is advised to use
     *         {@link HttpManagerClient#preparePost(String)} under the hood.
     */
    @NotNull
    default BoundRequestBuilder prepareScrapPost(@NotNull final String url) {
        return prepareScrapPost(url, Map.of());
    }


    /**
     * <p>
     *     Will prepare and build the corresponding {@link BoundRequestBuilder}
     *     POST HTTP request while respecting all contracts required by the
     *     current configurations.
     * </p>
     * @param url the url to scrap at.
     * @param fields The post fields to add to the request body.
     * @return the built and prepared {@link BoundRequestBuilder}. It is advised to use
     *         {@link HttpManagerClient#preparePost(String)} under the hood.
     */
    @NotNull
    BoundRequestBuilder prepareScrapPost(@NotNull final String url, @Nullable final Map<String, Object> fields);


    /**
     * <p>
     *     Will prepare and build the corresponding {@link BoundRequestBuilder}
     *     GET HTTP request while respecting all contracts required by the
     *     current configurations.
     * </p>
     * @param url the url to scrap at.
     * @return the built and prepared {@link BoundRequestBuilder}. It is advised to use
     *         {@link HttpManagerClient#preparePost(String)} under the hood.
     */
    @NotNull
    BoundRequestBuilder prepareScrapGet(@NotNull final String url);

    /**
     * @return the current {@link HttpMetrics} of the current scrapper instance.
     * @throws ScrapperUnsupportedException if this method is not supported by
     *                                      the current implementation.
     */
    @NotNull
    HttpMetrics getHttpMetrics() throws ScrapperUnsupportedException;

}
