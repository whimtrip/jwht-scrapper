package fr.whimtrip.ext.jwhtscrapper.enm;

import fr.whimtrip.ext.jwhtscrapper.annotation.RequestsConfig;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     This enumeration defines all possible pausing behaviors that can be
 *     taken when a warning sign is triggered.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 *
 */
public enum PausingBehavior {

    /**
     * Won't wait at all.
     */
    DONT_WAIT,

    /**
     * Will pause current failed scrap for a time delay
     * specified by {@link RequestsConfig#warningSignDelay()}.
     *
     */
    PAUSE_CURRENT_THREAD_ONLY,

    /**
     * Will pause all current running scraps for a time delay
     * specified by {@link RequestsConfig#warningSignDelay()}.
     * Once the delay is over, the scraps will progressively
     * start back.
     */
    PAUSE_ALL_THREADS
}
