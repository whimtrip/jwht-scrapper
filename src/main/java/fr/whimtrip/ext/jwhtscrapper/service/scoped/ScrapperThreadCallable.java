/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.ext.jwhtscrapper.annotation.Link;
import fr.whimtrip.ext.jwhtscrapper.exception.WarningSignException;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;
import org.asynchttpclient.BoundRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Created by LOUISSTEIMBERG on 22/11/2017.
 */
public class ScrapperThreadCallable<P, M> implements Callable<Object> {

    private static final Logger log = LoggerFactory.getLogger(ScrapperThreadCallable.class);

    private final P parentObject;
    private final ScrappingContext<?, P, M, ? extends ScrapperHelper<P, M>> context;
    private final RequestScrappingContext requestScrappingContext;
    private final ScrapperHelper<P, M> scrapperHelper;
    private final HtmlAutoScrapper<M> htmlAutoScrapper;

    private boolean done = false;
    private boolean scrapped = false;

    public ScrapperThreadCallable(
            final P parentObject,
            final ScrappingContext<?, P, M, ? extends ScrapperHelper<P, M>> context,
            final HtmlAutoScrapper<M> htmlAutoScrapper
    )
    {
        this.parentObject = parentObject;
        this.context = context;
        this.htmlAutoScrapper = htmlAutoScrapper;
        scrapperHelper = context.helper;
        requestScrappingContext = context.getRequestScrappingContext();
    }

    @Override
    public Object call() throws Exception {
        M model = null;
        try {
            if (scrapperHelper.shouldBeScrapped(parentObject)) {
                String url = scrapperHelper.createUrl(parentObject);
                BoundRequestBuilder req;

                if (requestScrappingContext.getMethod() == Link.Method.GET) {
                    req = htmlAutoScrapper.prepareScrapGet(url);
                } else {
                    req = htmlAutoScrapper.prepareScrapPost(url);
                }

                scrapperHelper.editRequest(req, parentObject);

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
                done = true;

                return scrapperHelper.returnResult(parentObject, model);
            }
            done = true;
            return null;
        }catch(Exception e)
        {
            scrapperHelper.handleException(e, parentObject, model);
            scrapped = true;
            done = true;
            throw e;
        }
    }

    public boolean isDone() {
        return done;
    }

    public boolean hasScrapped() {
        return scrapped;
    }
}
