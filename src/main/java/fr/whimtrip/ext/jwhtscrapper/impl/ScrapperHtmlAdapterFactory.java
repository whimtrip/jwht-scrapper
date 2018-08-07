package fr.whimtrip.ext.jwhtscrapper.impl;

import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhthtmltopojo.adapter.DefaultHtmlAdapterImpl;
import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlAdapter;
import fr.whimtrip.ext.jwhthtmltopojo.intrf.HtmlAdapterFactory;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.ScrapperHtmlAdapter;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     {@link HtmlAdapterFactory} implementation that will instanciate
 *     {@link ScrapperHtmlAdapter} instead of default {@link DefaultHtmlAdapterImpl}.
 *     This will allow the implementation of many surrounding features.
 * </p>
 *
 * @see ScrapperHtmlAdapter
 * @author Louis-wht
 * @since 1.0.0
 */
public class ScrapperHtmlAdapterFactory implements HtmlAdapterFactory {
    @Override
    public <T> HtmlAdapter<T> instanciateAdapter(HtmlToPojoEngine htmlToPojoEngine, Class<T> tClass) {
        return new ScrapperHtmlAdapter<>(htmlToPojoEngine, tClass);
    }
}
