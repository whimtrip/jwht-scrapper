package fr.whimtrip.ext.jwhtscrapper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhtscrapper.impl.DefaultExceptionLoggerService;
import fr.whimtrip.ext.jwhtscrapper.impl.ScrapperHtmlAdapterFactory;
import fr.whimtrip.ext.jwhtscrapper.intfr.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import org.asynchttpclient.AsyncHttpClient;

public class AutomaticScrapperManagerBuilder {

    private ExceptionLogger exceptionLogger;
    private HtmlToPojoEngine htmlToPojoEngine;
    private ObjectMapper objectMapper;
    private AsyncHttpClient asyncHttpClient;
    private ProxyFinder proxyFinder;

    public AutomaticScrapperManagerBuilder setExceptionLogger(ExceptionLogger exceptionLogger) {

        this.exceptionLogger = exceptionLogger;
        return this;
    }

    public AutomaticScrapperManagerBuilder setHtmlToPojoEngine(HtmlToPojoEngine htmlToPojoEngine) {

        this.htmlToPojoEngine = htmlToPojoEngine;
        return this;
    }

    public AutomaticScrapperManagerBuilder setObjectMapper(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
        return this;
    }

    public AutomaticScrapperManagerBuilder setAsyncHttpClient(AsyncHttpClient asyncHttpClient) {

        this.asyncHttpClient = asyncHttpClient;
        return this;
    }

    public AutomaticScrapperManagerBuilder setProxyFinder(ProxyFinder proxyFinder) {

        this.proxyFinder = proxyFinder;
        return this;
    }

    public AutomaticScrapperManager createAutomaticScrapperService() {
        this.exceptionLogger = exceptionLogger == null ? new DefaultExceptionLoggerService() : exceptionLogger;

        return new AutomaticScrapperManager(
                    new HtmlAutoScrapperManagerBuilder(
                            getOrBuildHtmlToPojoEngine(),
                            exceptionLogger
                    )
                        .setAsyncHttpClient(asyncHttpClient)
                        .setObjectMapper(objectMapper)
                        .setProxyFinder(proxyFinder)
                        .createHtmlAutoScrapperManager(),
                    exceptionLogger
        );
    }

    private HtmlToPojoEngine getOrBuildHtmlToPojoEngine() {
        if(htmlToPojoEngine == null)
            htmlToPojoEngine = HtmlToPojoEngine.create(new ScrapperHtmlAdapterFactory());
        return htmlToPojoEngine;
    }
}