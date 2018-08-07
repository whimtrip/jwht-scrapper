package fr.whimtrip.ext.jwhtscrapper.enm;

/**
 *
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 27/07/18</p>
 *
 * <p>
 *     Status a proxy can have. Statuses are used not to reuse proxies
 *     that has been flagged as non working. Most proxies don't always
 *     work properly so that it is recommended to store and update their
 *     status in order not to reuse non working ones.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public enum Status {

    /** Working Proxy */
    WORKING,
    /**
     * Shouldn't be used as of now but a process to make a proxy
     * change from Frozen to WORKING might be imagined
     */
    FROZEN,
    /** Not to be used anymore */
    BANNED;



}
