/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.core.util.intrf.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.enm.Method;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperException;
import fr.whimtrip.ext.jwhtscrapper.exception.WarningSignException;
import fr.whimtrip.ext.jwhtscrapper.intfr.HtmlAutoScrapper;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;
import fr.whimtrip.ext.jwhtscrapper.intfr.AutomaticScrapperClient;
import fr.whimtrip.ext.jwhtscrapper.service.base.BoundRequestBuilderProcessor;
import fr.whimtrip.ext.jwhtscrapper.service.base.ScrapperThreadCallable;
import fr.whimtrip.ext.jwhtscrapper.service.holder.RequestsScrappingContext;
import fr.whimtrip.ext.jwhtscrapper.service.holder.ScrappingContext;
import org.asynchttpclient.BoundRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;


/**
 *
 *
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     The {@link ScrapperThreadCallable} specific implementations that will
 *     trigger each single scrap operation from each unique item of the input
 *     parent objects list. This will use both the {@link ScrapperHelper}
 *     implementation provided by the end-user, and the {@link HtmlAutoScrapper}
 *     instance provided through the builders used to create the current
 *     {@link AutomaticScrapperClient}.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 * @param <P> P is the parent object which will be used each time to create the request and that
 *           in the end will be modified.
 * @param <M> M is the model on which the Html responses will be mapped
 */
public final class ScrapperThreadCallableImpl<P, M> implements ScrapperThreadCallable<P, M> {

    private static final Logger log = LoggerFactory.getLogger(ScrapperThreadCallableImpl.class);

    private final P parentObject;
    private final ScrappingContext<P, M, ? extends ScrapperHelper<P, M>> context;
    private final RequestsScrappingContext requestsScrappingContext;
    private final ScrapperHelper<P, M> scrapperHelper;
    private final HtmlAutoScrapper<M> htmlAutoScrapper;
    private final BoundRequestBuilderProcessor requestProcessor;
    private final ExceptionLogger exceptionLogger;


    private boolean done = false;
    private boolean scrapped = false;


    public ScrapperThreadCallableImpl(
            final P parentObject,
            final ScrappingContext<P, M, ? extends ScrapperHelper<P, M>> context,
            final HtmlAutoScrapper<M> htmlAutoScrapper,
            final BoundRequestBuilderProcessor requestProcessor,
            final ExceptionLogger exceptionLogger
    )
    {
        this.parentObject = parentObject;
        this.context = context;
        this.htmlAutoScrapper = htmlAutoScrapper;
        scrapperHelper = context.getHelper();
        requestsScrappingContext = context.getRequestsScrappingContext();
        this.requestProcessor = requestProcessor;
        this.exceptionLogger = exceptionLogger;
    }

    /**
     * <p>
     *     This implementation of {@link Callable#call()} method
     *     provides the core processing that will interact with
     *     both the {@link ScrapperHelper} provided by the end
     *     user and the {@link HtmlAutoScrapper} instance in
     *     order to interact and scrap properly the input
     *     parent object of type {@code <P>}, turn it into
     *     a {@code <M>} type object and finally return an
     *     Object.
     * </p>
     * @return the result of this scrapping returned by
     *         {@link ScrapperHelper#returnResult(Object, Object)}
     * @throws ScrapperException if any of the steps involved in
     *                   the scrapping throws an exception.
     */
    @Override
    public Object call() throws ScrapperException {
        M model = null;
        try {
            if (scrapperHelper.shouldBeScrapped(parentObject)) {
                String url = scrapperHelper.createUrl(parentObject);
                BoundRequestBuilder req;

                if (requestsScrappingContext.getMethod() == Method.GET) {
                    req = htmlAutoScrapper.prepareScrapGet(url);
                } else {
                    req = htmlAutoScrapper.prepareScrapPost(url);
                }

                scrapperHelper.editRequest(req, parentObject, requestProcessor);

                model = scrapperHelper.instanciateModel(parentObject);


                try {
                    model = htmlAutoScrapper.scrap(req, model);
                }
                catch(WarningSignException e) {
                    /*
                     *    We don't perform any action here because: fail was already logged
                     *    before exception was thrown and also because exception was thrown to stop
                     *    model scrapping execution
                     */
                }

                scrapperHelper.buildModel(parentObject, model);

                if (scrapperHelper.shouldBeSaved(parentObject, model)) {
                    scrapperHelper.save(parentObject, model);
                }

                scrapped = scrapperHelper.wasScrapped(parentObject, model);

                return scrapperHelper.returnResult(parentObject, model);
            }
            return null;
        }

        catch(Exception e)
        {
            exceptionLogger.logException(e);
            scrapperHelper.handleException(e, parentObject, model);
            scrapped = false;
            throw e;
        }

        finally
        {
            done = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDone() {
        return done;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasScrapped() {
        return scrapped;
    }
}
