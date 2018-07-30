package fr.whimtrip.ext.jwhtscrapper.intfr;

import fr.whimtrip.ext.jwhtscrapper.exception.LinkException;
import fr.whimtrip.ext.jwhtscrapper.exception.ModelBindingException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperUnsupportedException;
import fr.whimtrip.ext.jwhtscrapper.exception.WarningSignException;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 30/07/18</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface HtmlAutoScrapper<T> {


    @NotNull
    T scrap(@NotNull final BoundRequestBuilder req)
            throws ModelBindingException, LinkException, WarningSignException;

    @NotNull
    T scrap(@NotNull final BoundRequestBuilder req, @Nullable final T obj)
            throws ModelBindingException, LinkException, WarningSignException;

    @NotNull
    T scrapPost(@NotNull final String url, @Nullable final Map<String, Object> fields)
            throws ModelBindingException, LinkException, WarningSignException;

    @NotNull
    T scrapGet(@NotNull final String url)
            throws ModelBindingException, LinkException, WarningSignException;


    @NotNull
    BoundRequestBuilder prepareScrapPost(@NotNull final String url);

    @NotNull
    BoundRequestBuilder prepareScrapPost(@NotNull final String url, @Nullable final Map<String, Object> fields);

    @NotNull
    BoundRequestBuilder prepareScrapGet(@NotNull final String url);

    @NotNull
    HttpMetrics getHttpMetrics() throws ScrapperUnsupportedException;

}
