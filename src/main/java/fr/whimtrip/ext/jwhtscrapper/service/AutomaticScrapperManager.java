/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service;

import fr.whimtrip.core.util.exception.ObjectCreationException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapFailedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperException;
import fr.whimtrip.ext.jwhtscrapper.intfr.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by LOUISSTEIMBERG on 21/11/2017.
 */
public class AutomaticScrapperManager {

    private HtmlAutoScrapperManager htmlAutoScrapperManager;

    private ExceptionLogger exceptionLogger;

    AutomaticScrapperManager(HtmlAutoScrapperManager htmlAutoScrapperManager, ExceptionLogger exceptionLogger) {

        this.htmlAutoScrapperManager = htmlAutoScrapperManager;
        this.exceptionLogger = exceptionLogger;
    }

    @SuppressWarnings("unchecked")
    private <T> AutomaticScrapperBuilder<T> createBuilder(T parentObject)
    {
        return (AutomaticScrapperBuilder<T>) new AutomaticScrapperBuilder<>(parentObject.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T> AutomaticScrapperBuilder<T> createBuilder()
    {
        return (AutomaticScrapperBuilder<T>) new AutomaticScrapperBuilder<>();
    }

    @SuppressWarnings("unchecked")
    public <T>  AutomaticScrapperClient createClient(T parentObj, String... fields)
    {
        AutomaticScrapperBuilder<T> scrapperBuilder = createBuilder(parentObj);
        List<ScrappingContext> scrapperContexts;
        try {
            scrapperContexts = scrapperBuilder.prepareScrappers(parentObj, fields);
        }
        catch(IllegalAccessException e)
        {
            exceptionLogger.logException(e);
            throw new ObjectCreationException(e);
        }

        return buildAutomaticScrapperClient(scrapperContexts);
    }


    public <P> AutomaticScrapperClient createClient(List<P> parentObjs, Class<? extends ScrapperHelper<P, ?>> helperClazz) {
        if(parentObjs == null || parentObjs.isEmpty())
            throw new ScrapperException("Cannot scrap empty or null parent object list.");

        AutomaticScrapperBuilder builder = createBuilder();
        List<ScrappingContext> scrappingContexts = builder.prepareScrappers(parentObjs, helperClazz);

        return buildAutomaticScrapperClient(scrappingContexts);
    }


    @NotNull
    private AutomaticScrapperClient buildAutomaticScrapperClient(List<ScrappingContext> scrapperContexts) {


        Map<String, AutomaticFieldScrapperClient> scrappers = new HashMap<>();

        for(ScrappingContext context : scrapperContexts)
        {
            ProxyManagerClient proxyClient =
                    this.htmlAutoScrapperManager
                            .createProxyManagerClient(context.getRequestScrappingContext());
            HtmlAutoScrapper autoScrapper =
                    this.htmlAutoScrapperManager
                            .createHtmlAutoScrapper(
                                    proxyClient,
                                    context.getModelClazz(),
                                    context.getRequestScrappingContext().isThrowExceptions(),
                                    context.getRequestScrappingContext().getRequestsConfig().parallelizeLinkListPolling(),
                                    context.getRequestScrappingContext().getRequestsConfig().followRedirections(),
                                    context.getRequestScrappingContext().getRequestsConfig().warningSignDelay()
                            );

            AutomaticFieldScrapperClient client = new AutomaticFieldScrapperClient(context, autoScrapper, exceptionLogger);

            scrappers.put(context.getName(), client);

        }

        return new AutomaticScrapperClient(scrappers, exceptionLogger);
    }

    public static class AutomaticScrapperClient
    {
        private final Map<String, AutomaticFieldScrapperClient> scrapperClients;
        private boolean scrapped = false;

        private final Map<String, List> results = new HashMap<>();
        private final ExceptionLogger exceptionLogger;

        public AutomaticScrapperClient(Map<String, AutomaticFieldScrapperClient> scrapperClients, ExceptionLogger exceptionLogger) {
            this.scrapperClients = scrapperClients;
            this.exceptionLogger = exceptionLogger;
        }

        public Map<String, List> scrap() throws ScrapperException
        {
            if(!scrapped)
            {
                for (Map.Entry<String, AutomaticFieldScrapperClient> clientEntry : scrapperClients.entrySet())
                {
                    try {
                        results.put(clientEntry.getKey(), clientEntry.getValue().scrap());
                    }
                    catch(ExecutionException | InterruptedException e)
                    {
                        for (Map.Entry<String, AutomaticFieldScrapperClient> clientEntry1 : scrapperClients.entrySet())
                        {
                            clientEntry1.getValue().stopRunningTasks();
                        }
                        exceptionLogger.logException(e);
                        throw new ScrapFailedException(e);
                    }
                }
                scrapped = true;
            }

            else{
                throw new ScrapperException("AutomaticScrapperClient instance cannot be scrapped twice");
            }

            return results;
        }
    }


}
