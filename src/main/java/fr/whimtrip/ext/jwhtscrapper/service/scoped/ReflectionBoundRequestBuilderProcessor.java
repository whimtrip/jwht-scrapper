/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.core.util.WhimtripUtils;
import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.service.base.BoundRequestBuilderProcessor;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import org.asynchttpclient.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 06/08/18</p>
 *
 * <p>
 *     Default implementation of {@link BoundRequestBuilderProcessor}. Amongst
 *     ways to implement it described in the javadoc of the interface, this
 *     implementation uses, as suggested by its name, java reflection without
 *     caching.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public final class ReflectionBoundRequestBuilderProcessor implements BoundRequestBuilderProcessor {

    private static final Logger log = LoggerFactory.getLogger(ReflectionBoundRequestBuilderProcessor.class);

    private final ExceptionLogger exceptionLogger;


    private Field uriField, methodField, headersField, paramsField, cookiesField;


    /**
     * @param exceptionLogger the exception logger that will be used to report
     *                        reflection fields accesses exceptions. This processing
     *                        unit is not supposed to throw any exception because
     *                        if an exception occures, then it means that the code
     *                        base is not production ready. This is likely due to
     *                        a version problem.
     */
    public ReflectionBoundRequestBuilderProcessor(@NotNull final ExceptionLogger exceptionLogger)
    {
        this.exceptionLogger = exceptionLogger;
        paramsField = getReqField("formParams");
        headersField = getReqField("headers");
        uriField = getReqField("uri");
        cookiesField = getReqField("cookies");
        methodField = getReqField("method");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getMethod(@NotNull BoundRequestBuilder req)
    {
        try {
            return (String) WhimtripUtils.getObjectFromField(methodField, req);
        } catch (IllegalAccessException e) {
            exceptionLogger.logException(e);
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setOrReplaceHeader(@NotNull String name, @Nullable String value, @NotNull BoundRequestBuilder req) {
        setOrReplaceHeader(name, value, getHttpHeaders(req));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHeader(@NotNull String name, @Nullable String value, BoundRequestBuilder req)
    {

        HttpHeaders headers = getHttpHeaders(req);
        if(headers == null) {
            headers = new DefaultHttpHeaders();
            req.setHeaders(headers);
        }
        headers.add(name, value);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void printReq(@NotNull BoundRequestBuilder req)
    {
        if(!log.isDebugEnabled())
            return;

        StringBuilder sb = new StringBuilder();

        sb.append("Outputing request with method ")
          .append(getMethod(req))
          .append(" at url ")
          .append(getUrlFromRequestBuilder(req))
          .append("\n");

        List<String> printedElements = new ArrayList<>();
        printedElements.add(sb.toString());

        HttpHeaders headers = getHttpHeaders(req);
        if(headers != null)
            printHeaders(headers, printedElements);

        List<Param> params = getParam(req);
        if(params != null)
            printParams(params, printedElements);

        List<io.netty.handler.codec.http.cookie.Cookie> cookies = getCookies(req);
        if(cookies != null)
            printCookies(cookies, printedElements);

        synchronized (log) {
            for (String pe : printedElements)
                log.debug(pe);
        }
    }


    /**
     * @param name the name of the header to set / replace.
     * @param value the value of the header to set / replace.
     * @param actHeaders the headers to use to perform the
     *                   setting / replacing operation.
     */
    private void setOrReplaceHeader(String name, String value, HttpHeaders actHeaders) {
        if(actHeaders.getAll(name).isEmpty())
            actHeaders.add(name, value);
    }

    /**
     * @param req the request builder to extract the request url from.
     * @return the extracted url.
     */
    private String getUrlFromRequestBuilder(BoundRequestBuilder req)
    {

        try {
            return  WhimtripUtils.getObjectFromField(uriField, req).toString();
        }

        catch (IllegalAccessException e)
        {
            exceptionLogger.logException(e);
        }

        return "";
    }


    /**
     * @param req the request builder to extract http cookies from.
     * @return extracted http cookies.
     */
    @SuppressWarnings("unchecked")
    private List<io.netty.handler.codec.http.cookie.Cookie> getCookies(BoundRequestBuilder req) {

        try {
            return (List<io.netty.handler.codec.http.cookie.Cookie>) WhimtripUtils.getObjectFromField(cookiesField, req);
        } catch (IllegalAccessException e) {
            exceptionLogger.logException(e);
        }
        return null;
    }


    /**
     * @param req the request builder to extract http headers from.
     * @return extracted http headers.
     */
    private HttpHeaders getHttpHeaders(BoundRequestBuilder req)
    {
        try {
            return (HttpHeaders) WhimtripUtils.getObjectFromField(headersField, req);
        } catch (IllegalAccessException e) {
            exceptionLogger.logException(e);
        }
        return null;
    }


    /**
     * @param req the request builder to extract http post params from.
     * @return extracted http post params.
     */
    private List<Param> getParam(BoundRequestBuilder req)
    {
        try {
            return (List<Param>) WhimtripUtils.getObjectFromField(paramsField, req);
        } catch (IllegalAccessException e) {
            exceptionLogger.logException(e);
        }
        return null;
    }


    /**
     * @param name the field name to gather.
     * @return the corresponding java reflection {@link Field}
     */
    private Field getReqField(String name) {
        try {
            return RequestBuilderBase.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            exceptionLogger.logException(e);
        }
        return null;
    }


    /**
     * @param params the params to print.
     * @param printedElements the elements to append logs to.
     */
    private void printParams(List<Param> params, List<String> printedElements) {
        printedElements.add("Fields found on this request are : ");
        for(Param prm : params)
        {
            printedElements.add(prm.getName() + " : " + prm.getValue() );
        }
    }


    /**
     * @param headers the headers to print.
     * @param printedElements the elements to append logs to.
     */
    private void printHeaders(HttpHeaders headers, List<String> printedElements) {
        Iterator<Map.Entry<String, String>> iterator = headers.iteratorAsString();
        while(iterator.hasNext())
        {
            Map.Entry<String, String> header = iterator.next();
            printedElements.add(
                    String.format("%s : %s", header.getKey(), header.getValue())
            );
        }
    }

    /**
     * @param cookies the cookies to print.
     * @param printedElements the elements to append logs to.
     */
    private void printCookies(List<Cookie> cookies, List<String> printedElements) {

        for(io.netty.handler.codec.http.cookie.Cookie ck : cookies)
        {
            printedElements.add(
                    new StringBuilder()
                            .append(ck.name())
                            .append("=")
                            .append(ck.value())
                            .append("; path=")
                            .append(ck.path())
                            .append("; domain=")
                            .append(ck.domain())
                            .append("; Expires=")
                            .append(ck.maxAge())
                        .toString()
            );
        }
    }


}
