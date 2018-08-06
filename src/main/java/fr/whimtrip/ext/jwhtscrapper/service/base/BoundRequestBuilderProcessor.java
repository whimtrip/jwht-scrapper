package fr.whimtrip.ext.jwhtscrapper.service.base;

import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilderBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 06/08/18</p>
 *
 * <p>
 *     This interface defines the contracts any implementing class should follow.
 *     Because the {@link BoundRequestBuilder} class does not feature getters method
 *     and because the request object is passed around at several steps without
 *     further information, retrieving, accessing or editing some information is
 *     made impossible. That's why this processor class is required for the processing
 *     to become possible.
 * </p>
 *
 * <p>
 *     Values can be retrieved using several ways :
 * </p>
 * <ul>
 *     <li>
 *         Using {@link BoundRequestBuilder#build()} to retrieve a {@link Request}
 *         with fields that can be accessed but not retrieved. The problem with
 *         this implementation is that it is not really memory efficient (because
 *         new request object is created and computed each time and many inner arrays
 *         get instanciated) nor is it CPU efficient.
 *     </li>
 *     <li>
 *         Using Java reflection. It is memory efficient but not CPU efficient. it
 *         also is not very maintainable if the inner API of org.asynchttpclient
 *         changes someday.
 *     </li>
 *     <li>
 *         Using a cache provider matching unique request references with their
 *         respective context. This would require to add some methods to this interface
 *         or to still use one of the two above method to get all the values of
 *         a request builder.
 *     </li>
 *     <li>
 *         Using a wrapper of the builder with accessible fields but it would
 *         require to rewrite a lot of inner code.
 *     </li>
 *     <li>
 *         Requiring {@link RequestBuilderBase} instead of {@link BoundRequestBuilder}
 *         in the whole application and creating a custom implementing class with
 *         an equivalent {@link BoundRequestBuilder#execute()} method. Additionally,
 *         it would require to reproduce the code of {@link DefaultAsyncHttpClient#requestBuilder(String, String)}
 *         instead of using directly the {@link DefaultAsyncHttpClient#prepareGet(String)}
 *         method when preparing a request. This way, the custom {@link RequestBuilderBase}
 *         implementation could be used instead. This implementation would store internally
 *         all required vars in fields that could be directly accessed or that could enable
 *         the different minimum features required. This is so far the best option and it
 *         should be implemented in a later version.
 *     </li>
 * </ul>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface BoundRequestBuilderProcessor {


    /**
     * @param req the request to extract the method for
     * @return the method of the request.
     */
    String getMethod(@NotNull final BoundRequestBuilder req);

    /**
     * <p>Should set or replace a request header</p>
     * @param name the name of the header to set/replace.
     * @param value the value of the new header.
     * @param req the request to modify headers for.
     */
    void setOrReplaceHeader(@NotNull final String name, @Nullable final String value, @NotNull final BoundRequestBuilder req);

    /**
     * <p>
     *     Should add an header. If the header already exists,
     *     {@link HttpHeaders#add(String, Object)} should be used
     *     instead.
     * </p>
     * @param name the name of the header to add.
     * @param value the value of the new header.
     * @param req the request to modify headers for.
     */
    void addHeader(@NotNull final String name, @Nullable final String value, @NotNull final BoundRequestBuilder req);

    /**
     * <p>
     *     This method should log with debug marker {@link org.slf4j.Logger}
     *     the different informations contained in the request builder.
     *     This might includes headers, cookies, POST fields, url, method...
     * </p>
     * @param req the request to print.
     */
    void printReq(@NotNull final BoundRequestBuilder req);
}
