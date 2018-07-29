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

package fr.whimtrip.ext.jwhtscrapper.service;

import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhtscrapper.annotation.Header;
import fr.whimtrip.ext.jwhtscrapper.annotation.ProxyConfig;
import fr.whimtrip.ext.jwhtscrapper.annotation.RequestsConfig;
import fr.whimtrip.ext.jwhtscrapper.annotation.WarningSign;
import fr.whimtrip.ext.jwhtscrapper.intfr.BasicObjectMapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;
import fr.whimtrip.ext.jwhtscrapper.service.base.HttpManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.holder.PostField;
import fr.whimtrip.ext.jwhtscrapper.service.holder.RequestsScrappingContext;
import fr.whimtrip.ext.jwhtscrapper.service.holder.ScrappingContext;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.BoundRequestBuilderProcessor;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.DefaultHttpManagerClientBuilder;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.HtmlAutoScrapper;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.HttpWithProxyManagerClient;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.asynchttpclient.AsyncHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     This class will be in charge of providing factory method for instanciating
 *     both {@link HttpWithProxyManagerClient} and {@link HtmlAutoScrapper} thanks to
 *     methods input parameters and context services binded using the original
 *     constructor.
 * </p>
 *
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class HtmlAutoScrapperManager {

    private final ExceptionLogger exceptionLogger;

    private final HtmlToPojoEngine htmlToPojoEngine;

    private final BasicObjectMapper objectMapper;

    private final AsyncHttpClient asyncHttpClient;

    private final ProxyFinder proxyFinder;

    private final BoundRequestBuilderProcessor boundRequestBuilderProcessor;

    /**
     * Package private constructor that is yet only meant to be used through its
     * dedicated builder {@link HtmlAutoScrapperManagerBuilder}.
     * @param exceptionLogger the exception logger that will be used by both the
     *                        {@link HttpWithProxyManagerClient} and the {@link HtmlAutoScrapper}
     * @param htmlToPojoEngine the core html to pojo engine allowing us to parse
     *                         HTML input to java POJOs.
     * @param objectMapper the object mapper to use for mapping differently formatted
     *                     strings.
     * @param asyncHttpClient the http client to use for performing the requests
     * @param proxyFinder the proxy finder to take full usage of proxies configuration
     * @param boundRequestBuilderProcessor the request processor used for headers,
     *                                    cookies etc modfications as well as other
     *                                    eventual use cases.
     */
    HtmlAutoScrapperManager(
            @NotNull final ExceptionLogger exceptionLogger,
            @NotNull final HtmlToPojoEngine htmlToPojoEngine,
            @Nullable final BasicObjectMapper objectMapper,
            @NotNull final AsyncHttpClient asyncHttpClient,
            @Nullable final ProxyFinder proxyFinder,
            @NotNull final BoundRequestBuilderProcessor boundRequestBuilderProcessor
    ){
        this.exceptionLogger = exceptionLogger;
        this.htmlToPojoEngine = htmlToPojoEngine;
        this.objectMapper = objectMapper;
        this.asyncHttpClient = asyncHttpClient;
        this.proxyFinder = proxyFinder;
        this.boundRequestBuilderProcessor = boundRequestBuilderProcessor;
    }

    /**
     *
     * Simplified factory method.
     *
     * @param awaitBetweenRequests time to wait between each consecutive http
     *                             request.
     * @param proxyChangeRate the rate at which the proxies should be switched
     * @param timeout the timeout in milliseconds before the request will be
     *                retried
     * @param useProxy wether you should use proxies or not for performing your
     *                request
     * @param maxRequestRetries maximum number of retries before throwing a
     *                          failure exception
     * @return built {@link HttpWithProxyManagerClient}
     */
    public HttpManagerClient createProxyManagerClient(
            int awaitBetweenRequests,
            int proxyChangeRate,
            int timeout,
            boolean useProxy,
            int maxRequestRetries
    ){
        return createProxyManagerClient(
                awaitBetweenRequests,
                proxyChangeRate,
                timeout,
                useProxy,
                false,
                true,
                false,
                true,
                maxRequestRetries,
                null,
                null,
                new Cookie[]{}
        );
    }

    /**
     *
     * @param awaitBetweenRequests time to wait between each consecutive http
     *                             request. <p></p>
     *
     * @param proxyChangeRate the rate at which the proxies should be switched
     *
     * @param timeout the timeout in milliseconds before the request will be
     *                retried <p></p>
     *
     * @param useProxy wether you should use proxies or not for performing your
     *                request<p></p>
     *
     * @param connectToProxyBeforeRequest wether a {@code CONNECT} TCP initialization
     * request should be performed before hand.
     * <strong>Warning! Only use if you know what you are doing!</strong><p></p>
     *
     * @param rotatingUserAgent will auto assign rotating user agent headers to
     *  each request using {@link RotatingUserAgent#pickRandomUserAgent()}.<p></p>
     *
     * @param allowInfiniteRedirections will allow infinite redirections.
     *  Redirections with {@code 301} or {@code 302} HTTP Status codes will
     *  be followed as a normal browser would. Redirections are by default
     *  limited to 3 on the same request. Setting this field to true will
     *  let potential (quite common case when scrapping) happens.
     *  <strong>Warning! Only use if you know what you are doing!</strong>
     *
     * @param followRedirections wether HTTP redirection (301 and 302 HTTP status)
     * should be accepted or not. If false, no redirection will be followed, even
     * though {@code allowInfiniteRedirections} is set to true. If set to true with
     * {@code allowInfiniteRedirections} set to false, redirections will only be
     * followed once in per single HTTP request but not more.
     *
     * @param maxRequestRetries maximum number of retries before throwing a
     *                          failure exception<p></p>
     *
     *
     * @param headers default headers to use in each requests<p></p>
     * @param cookies default cookies to use in each requests<p></p>
     * @param fields default POST fields to use on each requests.
     *
     * @return built {@link HttpWithProxyManagerClient}
     */
    public HttpManagerClient createProxyManagerClient(
            int awaitBetweenRequests,
            int proxyChangeRate,
            int timeout,
            boolean useProxy,
            boolean connectToProxyBeforeRequest,
            boolean rotatingUserAgent,
            boolean allowInfiniteRedirections,
            boolean followRedirections,
            int maxRequestRetries,
            HttpHeaders headers,
            List<PostField> fields,
            Cookie... cookies
    ){

        return
                new DefaultHttpManagerClientBuilder(asyncHttpClient, exceptionLogger, boundRequestBuilderProcessor)
                        .setAwaitBetweenRequests(awaitBetweenRequests)
                        .setProxyChangeRate(proxyChangeRate)
                        .setTimeout(timeout)
                        .setUseProxy(useProxy)
                        .setConnectToProxyBeforeRequest(connectToProxyBeforeRequest)
                        .setRotatingUserAgent(rotatingUserAgent)
                        .setAllowInfiniteRedirections(allowInfiniteRedirections)
                        .setFollowRedirections(followRedirections)
                        .setMaxRequestRetries(maxRequestRetries)
                        .setDefaultHeaders(headers)
                        .setDefaultFields(fields)
                        .setDefaultCookies(cookies)
                        .setProxyFinder(proxyFinder)
                        .build();
    }

    /**
     * <p>Manual {@link HtmlAutoScrapper} factory method.</p>
     * @param client the {@link HttpWithProxyManagerClient} that will be used under the
     *               hood by the {@link HtmlAutoScrapper}.<p></p>
     *
     * @param clazz the class to map resulting outputs to.<p></p>
     *
     * @param throwEx wether exceptions for link lists concurrent scrapping
     *                should be thrown or catched.<p></p>
     *
     * @param followRediretcions wether HTTP redirections should be followed
     *                           or not (HTTP redirections is valid if status
     *                           code is {@code 301} or {@code 302} and when
     *                           the {@code Location} header is not empty.<p></p>
     *
     * @param warningSignDelay delay before retrying any action in the case
     *                         a {@link WarningSign} was triggered and only if it
     *                         was set to {@link WarningSign.Action#RETRY}.<p></p>
     * @param <T> the type of model this scrapper will cast resulting outputs to.
     * @return built in {@link HtmlAutoScrapper}.
     */
    public <T> HtmlAutoScrapper<T> createHtmlAutoScrapper(
            final HttpManagerClient client,
            Class<T> clazz,
            boolean throwEx,
            boolean followRediretcions,
            int warningSignDelay
    )
    {
        return new HtmlAutoScrapper<>(
                client,
                htmlToPojoEngine,
                boundRequestBuilderProcessor,
                objectMapper,
                clazz,
                throwEx,
                followRediretcions,
                warningSignDelay
        );
    }

    /**
     * <p>
     *     Automatic factory method using {@link RequestsScrappingContext}
     *     built using annotations gathered on top of {@link ScrapperHelper}
     *     implementation.
     * </p>
     * @param requestPreparator context of the scrapping request
     * @return built in {@link HttpWithProxyManagerClient}
     */
    public HttpManagerClient createProxyManagerClient(RequestsScrappingContext requestPreparator) {
        RequestsConfig config = requestPreparator.getRequestsConfig();
        ProxyConfig proxyConfig = config.proxyConfig();

        HttpHeaders headers = new DefaultHttpHeaders();

        for(Header hdr : config.defaultHeaders())
        {
            headers.add(hdr.name(), hdr.value());
        }


        List<Cookie> cookieList = new ArrayList<>();

        for(fr.whimtrip.ext.jwhtscrapper.annotation.Cookie ck : config.defaultCookies())
        {
            Cookie cookie = new DefaultCookie(ck.name(), ck.value());
            cookie.setDomain(ck.domain());
            cookie.setPath(ck.path());
            cookie.setMaxAge(Cookie.UNDEFINED_MAX_AGE);
            cookieList.add(cookie);
        }

        Cookie[] cookies = new Cookie[cookieList.size()];
        cookieList.toArray(cookies);

        List<PostField> fields = new ArrayList<>();

        for(fr.whimtrip.ext.jwhtscrapper.annotation.Field fld : config.defaultPostFields()) {
            fields.add(new PostField(fld.name(), fld.value()));
        }

        return createProxyManagerClient(
                config.waitBetweenRequests(),
                proxyConfig.proxyChangeRate(),
                config.timeout(),
                proxyConfig.useProxy(),
                proxyConfig.connectToProxyBeforeRequest(),
                config.rotatingUserAgent(),
                config.allowInfiniteRedirections(),
                config.followRedirections(),
                config.maxRequestRetries(),
                headers,
                fields,
                cookies
        );
    }

    /**
     * <p>
     *     Automatic factory method using {@link RequestsScrappingContext}
     *     built using annotations gathered on top of {@link ScrapperHelper}
     *     implementation.
     *     Built {@link HttpWithProxyManagerClient} is already required in order
     *     for this {@link HtmlAutoScrapper} to have the correct subjacent
     *     processing unit.
     * </p>
     * @param context  context of the scrapping request
     * @param httpManagerClient previously built in {@link HttpWithProxyManagerClient}
     *                    using this factory class.
     * @return built in {@link HtmlAutoScrapper}
     */
    public HtmlAutoScrapper createHtmlAutoScrapper(HttpManagerClient httpManagerClient, ScrappingContext context) {

        return createHtmlAutoScrapper(
                httpManagerClient,
                context.getModelClazz(),
                context.getRequestsScrappingContext().isThrowExceptions(),
                context.getRequestsScrappingContext().getRequestsConfig().followRedirections(),
                context.getRequestsScrappingContext().getRequestsConfig().warningSignDelay()
        );
    }
}
