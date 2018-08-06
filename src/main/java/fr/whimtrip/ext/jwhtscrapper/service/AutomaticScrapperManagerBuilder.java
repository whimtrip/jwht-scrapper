package fr.whimtrip.ext.jwhtscrapper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlAdapterFactory;
import fr.whimtrip.ext.jwhtscrapper.impl.DefaultExceptionLoggerService;
import fr.whimtrip.ext.jwhtscrapper.impl.ScrapperHtmlAdapterFactory;
import fr.whimtrip.ext.jwhtscrapper.intfr.BasicObjectMapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import fr.whimtrip.ext.jwhtscrapper.service.base.AutomaticScrapperManager;
import fr.whimtrip.ext.jwhtscrapper.service.base.BoundRequestBuilderProcessor;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.ReflectionBoundRequestBuilderProcessor;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

/**
 *
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     This builder class will instanciate an {@link AutomaticScrapperManager}
 *     with {@link AutomaticScrapperManagerImpl} implementation.
 * </p>
 *
 * <p>This builder let's you customize several parameters :</p>
 *
 * <ul>
 *     <li>
 *         {@link ExceptionLogger} you can provide your own implementations of
 *         an exception logger. This can be used to store exceptions in a database,
 *         or count them in order to trigger scrapper termination for example.
 *         Default implementation of this class provided is {@link DefaultExceptionLoggerService}
 *         and will be used if none is provided.
 *     </li>
 *     <li>
 *         {@link HtmlToPojoEngine} can let you use your own HtmlToPojoEngine.
 *         Those engines comes from our HTML to POJO framework and you can learn
 *         more about it <a href="https://github.com/whimtrip/jwht-htmltopojo">here</a>.
 *         Default implentation used if not provided features a custom {@link HtmlAdapterFactory}
 *         that will had new features to raw Html to Pojo lib to support link following
 *         and warning signs. see {@link ScrapperHtmlAdapterFactory}.
 *     </li>
 *     <li>
 *         {@link BasicObjectMapper} can let you map resulting String from another kind of
 *         input than HTML If not null, Html To Pojo Engine won't convert the strings to
 *         POJO so be careful with this. If {@code jsonScrapper} is set to true, a default
 *         Jackson Wraper implementation of this Object Mapper {@link DefaultBasicObjectMapper}
 *         will be provided in order to accept Json strings instead of HTML strings. The
 *         POJOs you'll use in this case must have valid Jackson Annotations.
 *
 *         This Object Mapper could be use to read and parse XML messages or any other
 *         kind of string input depending on your use case.
 *     </li>
 *     <li>
 *         {@link AsyncHttpClient} from asynchttp lib. Default implementation uses
 *         {@link DefaultAsyncHttpClientConfig.Builder} builder class. You can provide
 *         an AsyncHttpClient with different default parameters if needed.
 *     </li>
 *     <li>
 *         {@link ProxyFinder} is an interface that let's you pick proxies. For our private
 *         use case for example, we implemented it as a Proxy SQL entity Repository Wrapper.
 *         If you activated usage of proxies, you must provide a valid implementation of
 *         a Proxy Finder.
 *     </li>
 *     <li>
 *         {@code jsonScrapper} : has explained above, will use a Jackson wrapper of an
 *         {@link ObjectMapper} as an implementation so that JSON messages can be parsed
 *         as well.
 *     </li>
 *     <li>
 *         {@code requestProcessor} : The {@link BoundRequestBuilder} processor implementation.
 *         {@link BoundRequestBuilderProcessor}. Default implementation should fit all requirements.
 *     </li>
 * </ul>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class AutomaticScrapperManagerBuilder {

    private ExceptionLogger exceptionLogger;
    private HtmlToPojoEngine htmlToPojoEngine;
    private BasicObjectMapper objectMapper;
    private AsyncHttpClient asyncHttpClient;
    private ProxyFinder proxyFinder;
    private boolean jsonScrapper = false;
    private BoundRequestBuilderProcessor requestProcessor;

    /**
     * @param exceptionLogger {@link ExceptionLogger} you can provide your own implementations
     *                        an exception logger. This can be used to store exceptions in a database,
     *                        or count them in order to trigger scrapper termination for example.
     *                        Default implementation of this class provided is {@link DefaultExceptionLoggerService}
     *                        and will be used if none is provided.
     *
     * @return the same instance of {@link AutomaticScrapperManagerBuilder}.
     */
    public AutomaticScrapperManagerBuilder setExceptionLogger(ExceptionLogger exceptionLogger) {

        this.exceptionLogger = exceptionLogger;
        return this;
    }

    /**
     *
     * @param htmlToPojoEngine
     *         {@link HtmlToPojoEngine} can let you use your own HtmlToPojoEngine.
     *         Those engines comes from our HTML to POJO framework and you can learn
     *         more about it <a href="https://github.com/whimtrip/jwht-htmltopojo">here</a>.
     *         Default implentation used if not provided features a custom {@link HtmlAdapterFactory}
     *         that will had new features to raw Html to Pojo lib to support link following
     *         and warning signs. see {@link ScrapperHtmlAdapterFactory}.
     * @return the same instance of {@link AutomaticScrapperManagerBuilder}.
     */
    public AutomaticScrapperManagerBuilder setHtmlToPojoEngine(HtmlToPojoEngine htmlToPojoEngine) {

        this.htmlToPojoEngine = htmlToPojoEngine;
        return this;
    }


    /**
     *
     * @param objectMapper
     *         {@link BasicObjectMapper} can let you map resulting String from another kind of
     *         input than HTML If not null, Html To Pojo Engine won't convert the strings to
     *         POJO so be careful with this. If {@code jsonScrapper} is set to true, a default
     *         Jackson Wraper implementation of this Object Mapper {@link DefaultBasicObjectMapper}
     *         will be provided in order to accept Json strings instead of HTML strings. The
     *         POJOs you'll use in this case must have valid Jackson Annotations.
     *
     *         This Object Mapper could be use to read and parse XML messages or any other
     *         kind of string input depending on your use case.
     * @return the same instance of {@link AutomaticScrapperManagerBuilder}.
     */
    public AutomaticScrapperManagerBuilder setObjectMapper(BasicObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
        return this;
    }

    /**
     *
     * @param asyncHttpClient
     *         {@link AsyncHttpClient} from asynchttp lib. Default implementation uses
     *         {@link DefaultAsyncHttpClientConfig.Builder} builder class. You can provide
     *         an AsyncHttpClient with different default parameters if needed.
     * @return the same instance of {@link AutomaticScrapperManagerBuilder}.
     */
    public AutomaticScrapperManagerBuilder setAsyncHttpClient(AsyncHttpClient asyncHttpClient) {

        this.asyncHttpClient = asyncHttpClient;
        return this;
    }

    /**
     * @param proxyFinder
     *         {@link ProxyFinder} is an interface that let's you pick proxies. For our private
     *         use case for example, we implemented it as a Proxy SQL entity Repository Wrapper.
     *         If you activated usage of proxies, you must provide a valid implementation of
     *         a Proxy Finder.
     * @return the same instance of {@link AutomaticScrapperManagerBuilder}.
     */
    public AutomaticScrapperManagerBuilder setProxyFinder(ProxyFinder proxyFinder) {

        this.proxyFinder = proxyFinder;
        return this;
    }

    /**
     *
     * @param jsonScrapper
     *         {@code jsonScrapper} : has explained above, will use a Jackson wrapper of an
     *         {@link ObjectMapper} as an implementation so that JSON messages can be parsed
     *         as well.
     * @see #setObjectMapper(BasicObjectMapper)
     * @return the same instance of {@link AutomaticScrapperManagerBuilder}.
     */
    public AutomaticScrapperManagerBuilder setJsonScrapper(boolean jsonScrapper) {
        this.jsonScrapper = jsonScrapper;
        return this;
    }

    /**
     * @param requestProcessor The {@link BoundRequestBuilder} processor implementation.
     *         {@link BoundRequestBuilderProcessor}.
     * @return the same instance of {@link AutomaticScrapperManagerBuilder}.
     */
    public AutomaticScrapperManagerBuilder setRequestProcessor(BoundRequestBuilderProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
        return this;
    }

    /**
     * @return the built {@link AutomaticScrapperManagerImpl}
     */
    public AutomaticScrapperManager build() {
        this.exceptionLogger = exceptionLogger == null ? new DefaultExceptionLoggerService() : exceptionLogger;

        return new AutomaticScrapperManagerImpl(
                    new HtmlAutoScrapperManagerBuilder(
                            getOrBuildHtmlToPojoEngine(),
                            getOrBuildExceptionLogger()
                        )
                        .setAsyncHttpClient(asyncHttpClient)
                        .setObjectMapper(getOrBuildObjectMapper())
                        .setProxyFinder(proxyFinder)
                        .setBoundRequestBuilderProcessor(getOrBuildRequestProcessor())
                        .build(),
                    getOrBuildExceptionLogger(),
                    requestProcessor
        );
    }

    /**
     * @return submitted {@code htmlToPojoEngine} if not null, otherwise will build a
     *         new default one.
     */
    private HtmlToPojoEngine getOrBuildHtmlToPojoEngine() {
        if(htmlToPojoEngine == null)
            htmlToPojoEngine = HtmlToPojoEngine.create(new ScrapperHtmlAdapterFactory());
        return htmlToPojoEngine;
    }

    /**
     * @return submitted {@code jsonScrapper} if not null, otherwise, either a {@link DefaultBasicObjectMapper}
     *         wrapping a Jackson {@link ObjectMapper} to convert JSON strings to POJOs if {@code jsonScrapper}
     *         is set to true or a null value otherwise (default behavior).
     */
    private BasicObjectMapper getOrBuildObjectMapper() {
        return jsonScrapper && objectMapper == null ? new DefaultBasicObjectMapper() : objectMapper;
    }


    /**
     * @return submitted {@code requestProcessor} if not null, otherwise will build a
     *         new default one.
     */
    private BoundRequestBuilderProcessor getOrBuildRequestProcessor() {
        if(requestProcessor == null)
            requestProcessor = new ReflectionBoundRequestBuilderProcessor(getOrBuildExceptionLogger());
        return requestProcessor;
    }

    /**
     * @return submitted {@code exceptionLoggerService} if not null, otherwise will build a
     *         new default one.
     */
    private ExceptionLogger getOrBuildExceptionLogger() {
        if(exceptionLogger == null)
            exceptionLogger = new DefaultExceptionLoggerService();
        return exceptionLogger;
    }
}