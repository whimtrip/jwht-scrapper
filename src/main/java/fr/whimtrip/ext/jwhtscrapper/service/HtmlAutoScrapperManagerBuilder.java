package fr.whimtrip.ext.jwhtscrapper.service;

import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhtscrapper.intfr.BasicObjectMapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.BoundRequestBuilderProcessor;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.jetbrains.annotations.NotNull;

public class HtmlAutoScrapperManagerBuilder {

    private static final int DEFAULT_CONNECT_TIMEOUT = 1500;
    private static final int DEFAULT_REQUEST_TIMEOUT = 20_000;

    private final HtmlToPojoEngine htmlToPojoEngine;
    private final ExceptionLogger exceptionLogger;

    private BasicObjectMapper objectMapper;
    private AsyncHttpClient asyncHttpClient;
    private ProxyFinder proxyFinder;
    private BoundRequestBuilderProcessor boundRequestBuilderProcessor;

    public HtmlAutoScrapperManagerBuilder(@NotNull HtmlToPojoEngine htmlToPojoEngine, @NotNull ExceptionLogger exceptionLogger) {
        this.htmlToPojoEngine = htmlToPojoEngine;
        this.exceptionLogger = exceptionLogger;
    }


    public HtmlAutoScrapperManagerBuilder setObjectMapper(BasicObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
        return this;
    }

    public HtmlAutoScrapperManagerBuilder setAsyncHttpClient(AsyncHttpClient asyncHttpClient) {

        this.asyncHttpClient = asyncHttpClient;
        return this;
    }

    public HtmlAutoScrapperManagerBuilder setProxyFinder(ProxyFinder proxyFinder) {

        this.proxyFinder = proxyFinder;
        return this;
    }

    public HtmlAutoScrapperManagerBuilder setBoundRequestBuilderProcessor(BoundRequestBuilderProcessor boundRequestBuilderProcessor) {

        this.boundRequestBuilderProcessor = boundRequestBuilderProcessor;
        return this;
    }

    public HtmlAutoScrapperManager createHtmlAutoScrapperManager() {
        this.asyncHttpClient = asyncHttpClient == null ? buildAsyncHttpClient() : asyncHttpClient;
        this.boundRequestBuilderProcessor = boundRequestBuilderProcessor == null ?
                new BoundRequestBuilderProcessor(proxyFinder, exceptionLogger) :
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

    private AsyncHttpClient buildAsyncHttpClient() {
        return new DefaultAsyncHttpClient(
                new DefaultAsyncHttpClientConfig.Builder()
                        .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                        .setRequestTimeout(DEFAULT_REQUEST_TIMEOUT)
                        .build()
        );
    }

}