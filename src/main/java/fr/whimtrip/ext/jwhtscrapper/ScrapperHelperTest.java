package fr.whimtrip.ext.jwhtscrapper;

import fr.whimtrip.ext.jwhtscrapper.exception.ModelBindingException;
import fr.whimtrip.ext.jwhtscrapper.exception.UrlCreationException;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;
import fr.whimtrip.ext.jwhtscrapper.service.base.BoundRequestBuilderProcessor;
import org.asynchttpclient.BoundRequestBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 08/08/18</p>
 *
 * @author Louis-wht
 * @since TODO : ${PROJECT_VERSION}
 */
public class ScrapperHelperTest implements ScrapperHelper{
    @Override
    public boolean shouldBeScrapped(@NotNull Object parent) {

        return false;
    }

    @Override
    public String createUrl(@NotNull Object parent) throws UrlCreationException {

        return null;
    }

    @Override
    public void editRequest(@NotNull BoundRequestBuilder req, @NotNull Object parent, @NotNull BoundRequestBuilderProcessor requestProcessor) {

    }

    @Override
    public Object instanciateModel(@NotNull Object parent) {

        return null;
    }

    @Override
    public void buildModel(@NotNull Object parent, @NotNull Object model) throws ModelBindingException {

    }

    @Override
    public boolean shouldBeSaved(@NotNull Object parent, Object model) {

        return false;
    }

    @Override
    public void save(@NotNull Object parentObject, Object model) {

    }

    @Override
    public boolean wasScrapped(@NotNull Object parent, @NotNull Object model) {

        return false;
    }
}
