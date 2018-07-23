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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhtscrapper.annotation.Header;
import fr.whimtrip.ext.jwhtscrapper.annotation.ProxyConfig;
import fr.whimtrip.ext.jwhtscrapper.annotation.RequestsConfig;
import fr.whimtrip.ext.jwhtscrapper.intfr.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.HtmlAutoScrapper;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.ProxyManagerClient;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.ProxyManagerClientBuilder;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.RequestScrappingContext;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.asynchttpclient.AsyncHttpClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LOUISSTEIMBERG on 18/11/2017.
 */
public class HtmlAutoScrapperManager {

    private ExceptionLogger exceptionLogger;

    private HtmlToPojoEngine htmlToPojoEngine;

    private ObjectMapper objectMapper;

    private AsyncHttpClient asyncHttpClient;

    private ProxyFinder proxyFinder;

    private BoundRequestBuilderProcessor boundRequestBuilderProcessor;

    HtmlAutoScrapperManager(ExceptionLogger exceptionLogger, HtmlToPojoEngine htmlToPojoEngine, ObjectMapper objectMapper, AsyncHttpClient asyncHttpClient, ProxyFinder proxyFinder, BoundRequestBuilderProcessor boundRequestBuilderProcessor) {
        this.exceptionLogger = exceptionLogger;
        this.htmlToPojoEngine = htmlToPojoEngine;
        this.objectMapper = objectMapper;
        this.asyncHttpClient = asyncHttpClient;
        this.proxyFinder = proxyFinder;
        this.boundRequestBuilderProcessor = boundRequestBuilderProcessor;
    }

    public ProxyManagerClient createProxyManagerClient(
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
                maxRequestRetries,
                null,
                new Cookie[]{}
        );
    }

    public ProxyManagerClient createProxyManagerClient(
            int awaitBetweenRequests,
            int proxyChangeRate,
            int timeout,
            boolean useProxy,
            boolean connectToProxyBeforeRequest,
            int maxRequestRetries,
            HttpHeaders headers,
            Cookie... cookies
    )
    {

        return
                new ProxyManagerClientBuilder(asyncHttpClient, objectMapper, exceptionLogger, boundRequestBuilderProcessor)
                        .setAwaitBetweenRequests(awaitBetweenRequests)
                        .setProxyChangeRate(proxyChangeRate)
                        .setTimeout(timeout)
                        .setUseProxy(useProxy)
                        .setConnectToProxyBeforeRequest(connectToProxyBeforeRequest)
                        .setMaxRequestRetries(maxRequestRetries)
                        .setDefaultHeaders(headers)
                        .setDefaultCookies(cookies)
                        .setProxyFinder(proxyFinder)
                        .createProxyManagerClient();
    }

    public <T> HtmlAutoScrapper<T> createHtmlAutoScrapper(
            final ProxyManagerClient client,
            Class<T> clazz,
            boolean throwEx,
            boolean parallelizeLinkListPolling,
            boolean followRediretcions,
            int warningSignDelay
    )
    {
        return new HtmlAutoScrapper<>(client, htmlToPojoEngine, boundRequestBuilderProcessor, clazz, throwEx, parallelizeLinkListPolling, followRediretcions, warningSignDelay);
    }

    public ProxyManagerClient createProxyManagerClient(RequestScrappingContext requestPreparator) {
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

        return createProxyManagerClient(
                config.waitBetweenRequests(),
                proxyConfig.proxyChangeRate(),
                config.timeout(),
                proxyConfig.useProxy(),
                proxyConfig.connectToProxyBeforeRequest(),
                config.maxRequestRetries(),
                headers,
                cookies
        );
    }


}
