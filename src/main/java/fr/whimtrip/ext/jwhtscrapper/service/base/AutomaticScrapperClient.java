package fr.whimtrip.ext.jwhtscrapper.service.base;

import fr.whimtrip.ext.jwhtscrapper.exception.ScrapFailedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapNotFinishedException;
import fr.whimtrip.ext.jwhtscrapper.exception.ScrapperException;
import fr.whimtrip.ext.jwhtscrapper.service.scoped.ScrappingStats;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface AutomaticScrapperClient<P, M> {


    void scrap() throws ScrapperException;


    void addObjectsToScrap(List<P> l);

    ScrappingStats getScrappingStats();

    boolean isScrapped();


    List<M> getResults() throws ScrapFailedException, ScrapNotFinishedException;

    List<M> getResults(Long timeout, TimeUnit timeUnit) throws ScrapFailedException, ScrapNotFinishedException;

    void terminate();

}
