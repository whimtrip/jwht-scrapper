package fr.whimtrip.ext.jwhtscrapper.enm;

import fr.whimtrip.ext.jwhtscrapper.annotation.Scrapper;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     This enumeration defines all possible actions that can be taken when
 *     a warning sign is triggered.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 *
 */
public enum Action {

    /**
     * Retry the request.
     */
    RETRY,

    /**
     * Throw an exception. This will have the same impact
     * as {@link #STOP_ACTUAL_SCRAP} except that the scrap
     * will be accounted as a failure, and if {@link Scrapper#throwExceptions()}
     * is set to true, it will completely stop the whole
     * scrapping operation.
     */
    THROW_EXCEPTION,

    /**
     * Stop the actual scrap without further links explored
     * and returned the current model in its actual state.
     */
    STOP_ACTUAL_SCRAP,

    /**
     * Won't do anything : the scrap will pursue where it was
     * without our current POJO being further analyzed. It will
     * act almost as with {@link #STOP_ACTUAL_SCRAP} expect that
     * if the current scrapped POJO was itself a link followed,
     * the parent POJO scrap will continue.
     */
    NONE
}
