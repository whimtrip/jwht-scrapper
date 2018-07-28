package fr.whimtrip.ext.jwhtscrapper.service.base;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 28/07/18</p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface RequestChecker {



    Long getLastRequest();

    int getLastProxyChange();

    void incrementLastProxyChange();

    void checkAwaitBetweenRequest(String url);
}
