package fr.whimtrip.ext.jwhtscrapper.impl;

import fr.whimtrip.ext.jwhthtmltopojo.HtmlToPojoEngine;
import fr.whimtrip.ext.jwhthtmltopojo.adapter.HtmlAdapter;
import fr.whimtrip.ext.jwhthtmltopojo.intfr.HtmlAdapterFactory;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.ScrapperHtmlAdapter;

public class ScrapperHtmlAdapterFactory implements HtmlAdapterFactory {
    @Override
    public <T> HtmlAdapter<T> instanciateAdapter(HtmlToPojoEngine htmlToPojoEngine, Class<T> tClass) {
        return new ScrapperHtmlAdapter<>(htmlToPojoEngine, tClass);
    }
}
