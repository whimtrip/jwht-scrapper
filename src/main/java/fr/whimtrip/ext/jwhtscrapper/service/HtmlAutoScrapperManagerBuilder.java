package fr.whimtrip.ext.jwhtscrapper.service;

import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhtscrapper.intfr.BasicObjectMapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.HtmlAutoScrapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import fr.whimtrip.ext.jwhtscrapper.service.base.AutomaticScrapperManager;
import fr.whimtrip.ext.jwhtscrapper.service.base.BoundRequestBuilderProcessor;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.HttpWithProxyManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.ReflectionBoundRequestBuilderProcessor;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.jetbrains.annotations.NotNull;


/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     This builder will help building a custom {@link HtmlAutoScrapperManager}.
 *     With this manager class, you'll be able to instanciate manually your
 *     custom {@link HtmlAutoScrapper} or {@link HttpWithProxyManagerClient}.
 * </p>
 * <p>
 *     Usually, this class is created automatically through the native
 *     {@link AutomaticScrapperManager} implementation you will use but the two
 *     lastly mentionned classes can be used alone as well.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class HtmlAutoScrapperManagerBuilder {

    private static final int DEFAULT_CONNECT_TIMEOUT = 1500;
    private static final int DEFAULT_REQUEST_TIMEOUT = 20_000;

    private final HtmlToPojoEngine htmlToPojoEngine;
    private final ExceptionLogger exceptionLogger;

    private BasicObjectMapper objectMapper;
    private AsyncHttpClient asyncHttpClient;
    private ProxyFinder proxyFinder;
    private BoundRequestBuilderProcessor boundRequestBuilderProcessor;

    /**
     * Default Constructor. Requires at least :
     * @param htmlToPojoEngine an {@link HtmlToPojoEngine} to convert html messages to
     *                         POJOs.
     * @param exceptionLogger to log any incoming exception.
     */
    public HtmlAutoScrapperManagerBuilder(@NotNull HtmlToPojoEngine htmlToPojoEngine, @NotNull ExceptionLogger exceptionLogger) {
        this.htmlToPojoEngine = htmlToPojoEngine;
        this.exceptionLogger = exceptionLogger;
    }


    /**
     * @param objectMapper set your custom {@link BasicObjectMapper}. This will
     *                     automatically deactivate Html to POJO parsing altough
     *                     the {@link HtmlToPojoEngine} is still required.
     * @return the same instance of {@link HtmlAutoScrapperManagerBuilder}.
     */
    public HtmlAutoScrapperManagerBuilder setObjectMapper(BasicObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
        return this;
    }

    /**
     *
     * @param asyncHttpClient
     *         {@link AsyncHttpClient} from asynchttp lib. Default implementation uses
     *         {@link DefaultAsyncHttpClientConfig.Builder} builder class. You can provide
     *         an AsyncHttpClient with different default parameters if needed.
     * @return the same instance of {@link HtmlAutoScrapperManagerBuilder}.
     */
    public HtmlAutoScrapperManagerBuilder setAsyncHttpClient(AsyncHttpClient asyncHttpClient) {

        this.asyncHttpClient = asyncHttpClient;
        return this;
    }


    /**
     * @param proxyFinder
     *         {@link ProxyFinder} is an interface that let's you pick proxies. For our private
     *         use case for example, we implemented it as a Proxy SQL entity Repository Wrapper.
     *         If you activated usage of proxies, you must provide a valid implementation of
     *         a Proxy Finder.
     * @return the same instance of {@link HtmlAutoScrapperManagerBuilder}.
     */
    public HtmlAutoScrapperManagerBuilder setProxyFinder(ProxyFinder proxyFinder) {

        this.proxyFinder = proxyFinder;
        return this;
    }

    /**
     *
     * @param boundRequestBuilderProcessor the request editor processing unit.
     *                                     You can provide your own one.
     * @return the same instance of {@link HtmlAutoScrapperManagerBuilder}.
     */
    public HtmlAutoScrapperManagerBuilder setBoundRequestBuilderProcessor(BoundRequestBuilderProcessor boundRequestBuilderProcessor) {

        this.boundRequestBuilderProcessor = boundRequestBuilderProcessor;
        return this;
    }

    /**
     * @return Freshly built {@link HtmlAutoScrapperManager}.
     */
    public HtmlAutoScrapperManager build() {
        this.asyncHttpClient = asyncHttpClient == null ? buildAsyncHttpClient() : asyncHttpClient;
        this.boundRequestBuilderProcessor = boundRequestBuilderProcessor == null ?
                new ReflectionBoundRequestBuilderProcessor(exceptionLogger) :
                boundRequestBuilderProcessor;

        return new HtmlAutoScrapperManager(
                exceptionLogger,
                htmlToPojoEngine,
                objectMapper,
                asyncHttpClient,
                proxyFinder,
                boundRequestBuilderProcessor
        );
    }

    /**
     * @return Buiding the default {@link AsyncHttpClient} if not already
     *          submitted using {@link DefaultAsyncHttpClientConfig.Builder}
     *
     */
    private AsyncHttpClient buildAsyncHttpClient() {
        return new DefaultAsyncHttpClient(
                new DefaultAsyncHttpClientConfig.Builder()
                        .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                        .setRequestTimeout(DEFAULT_REQUEST_TIMEOUT)
                        .build()
        );
    }

}