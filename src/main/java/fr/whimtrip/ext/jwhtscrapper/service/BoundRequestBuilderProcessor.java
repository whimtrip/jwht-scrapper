/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service;

import fr.whimtrip.core.util.WhimtripUtils;
import fr.whimtrip.ext.jwhtscrapper.intfr.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.intfr.Proxy;
import fr.whimtrip.ext.jwhtscrapper.intfr.ProxyFinder;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.ProxyManagerClient;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Param;
import org.asynchttpclient.RequestBuilderBase;
import org.asynchttpclient.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by LOUISSTEIMBERG on 25/11/2017.
 */
public class BoundRequestBuilderProcessor {

    private static final Logger log = LoggerFactory.getLogger(BoundRequestBuilderProcessor.class);


    private final ProxyFinder proxyFinder;

    private final ExceptionLogger exceptionLogger;


    private Field proxyField, uriField, methodField, headersField, paramsField, cookiesField;


    public BoundRequestBuilderProcessor(@NotNull final ProxyFinder proxyFinder, ExceptionLogger exceptionLogger)
    {
        this.proxyFinder = proxyFinder;
        this.exceptionLogger = exceptionLogger;
    }

    private final Map<BoundRequestBuilder, HttpHeaders> headers = new HashMap<>();

    public String getUrlFromRequestBuilder(BoundRequestBuilder req)
    {
        if(uriField == null)
        {
            uriField = getReqField("uri");
        }


        try {
            return  WhimtripUtils.getObjectFromField(uriField, req).toString();
        }

        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return "";
    }


    public void printHeaders(HttpHeaders headers) {
        Iterator<Map.Entry<String, String>> iterator = headers.iteratorAsString();
        while(iterator.hasNext())
        {
            Map.Entry<String, String> header = iterator.next();
            log.info("{} : {}", header.getKey(), header.getValue());
        }
    }


    public Proxy getProxyServerFormRequestBuilder(BoundRequestBuilder req)
    {
        if(proxyField == null)
        {
            proxyField = getReqField("proxyServer");
        }

        try {
            ProxyServer prxSrv = WhimtripUtils.getObjectFromField(proxyField, req);
            if(prxSrv == null)
                return null;
            return proxyFinder.findOneByIp(prxSrv.getHost());
        }

        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }


    public String getMethod(BoundRequestBuilder req)
    {

        if(methodField == null)
        {
            methodField = getReqField("method");
        }
        try {
            return (String) WhimtripUtils.getObjectFromField(methodField, req);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void printReq(BoundRequestBuilder req)
    {
        log.info("Outputing request with method " + getMethod(req) + " at url " + getUrlFromRequestBuilder(req));
        HttpHeaders headers = getHttpHeaders(req);
        if(headers != null)
            printHeaders(headers);

        List<Param> params = getParam(req);
        if(params != null)
            printParams(params);

        List<io.netty.handler.codec.http.cookie.Cookie> cookies = getCookies(req);
        if(cookies != null)
            printCookies(cookies);

    }

    private void printCookies(List<Cookie> cookies) {

        for(io.netty.handler.codec.http.cookie.Cookie ck : cookies)
        {
            log.info(ck.name() + "=" + ck.value() + "; path=" + ck.path() + "; domain=" + ck.domain() + "; Expires=" + ck.maxAge());
        }
    }

    private List<io.netty.handler.codec.http.cookie.Cookie> getCookies(BoundRequestBuilder req) {
        if(cookiesField == null)
        {
            cookiesField = getReqField("cookies");
        }
        try {
            return (List<io.netty.handler.codec.http.cookie.Cookie>) WhimtripUtils.getObjectFromField(cookiesField, req);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void printParams(List<Param> params) {
        log.info("Fields found on this request are ? ");
        for(Param prm : params)
        {
            log.info(prm.getName() + " : " + prm.getValue() );
        }
    }

    public HttpHeaders getHttpHeaders(BoundRequestBuilder req)
    {

        if(headersField == null)
        {
            headersField = getReqField("headers");
        }
        try {
            return (HttpHeaders) WhimtripUtils.getObjectFromField(headersField, req);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Param> getParam(BoundRequestBuilder req)
    {

        if(paramsField == null)
        {
            paramsField = getReqField("formParams");
        }
        try {
            return (List<Param>) WhimtripUtils.getObjectFromField(paramsField, req);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Field getReqField(String name) {
        try {
            return RequestBuilderBase.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addUniqueHeader(String name, String value, BoundRequestBuilder req) {
        HttpHeaders actHeaders = headers.get(req);
        if(actHeaders == null)
        {
            actHeaders = getHttpHeaders(req);
            headers.put(req, actHeaders);
        }

        addUniqueHeader(name, value, actHeaders);
    }

    public void addUniqueHeader(String name, String value, HttpHeaders actHeaders) {
        if(actHeaders.getAll(name).isEmpty())
            actHeaders.add(name, value);
    }

    public BoundRequestBuilder recreateRequest(BoundRequestBuilder req, ProxyManagerClient proxyClient) {
        log.info("Recreating request : ");
        printReq(req);

        String method = getMethod(req);
        String url = getUrlFromRequestBuilder(req);
        BoundRequestBuilder newReq = null;
        if("GET".equals(method))
        {
            newReq = proxyClient.get(url);
        }
        else{
            newReq = proxyClient.post(url);
        }

        HttpHeaders headers = getHttpHeaders(req);
        for(Map.Entry<String, String> header : headers.entries())
        {
            addHeader(header.getKey(), header.getValue(), newReq);
        }

        return newReq;
    }

    public void addHeader( String name, String value, BoundRequestBuilder req)
    {

        HttpHeaders headers = getHttpHeaders(req);
        HttpHeaders newHeaders = newHttpHeaders(headers);
        newHeaders.add(name, value);
        req.setHeaders(newHeaders);
    }

    public HttpHeaders newHttpHeaders(HttpHeaders headers) {
        HttpHeaders newHeaders = new DefaultHttpHeaders();
        if(headers != null)
        {
            for (Map.Entry<String, String> header : headers.entries())
            {
                addUniqueHeader(header.getKey(), header.getValue(), newHeaders);
            }
        }
        return newHeaders;
    }
}
