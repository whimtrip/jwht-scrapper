package fr.whimtrip.ext.jwhtscrapper.intfr;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 27/07/18</p>
 *
 * <p>
 *     Scrapping Stats implementations will provide live
 *     statistics about an {@link AutomaticScrapperClient}
 *     scrapping state.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public interface ScrappingStats {


    /**
     * @return the number of finished tasks since the scrapping
     *         started. This will include the sum of both failed
     *         and successful scraps. It shall return the sum of
     *         {@link #getSuccessfullTasks()} and {@link #getFailedTasks()}.
     */
    int getFinishedTasks();


    /**
     * @return the number of tasks running right now or at the moment
     *         this object was instanciated.
     */
    int getRunningTasks();

    /**
     * @return the number of tasks finished successfully since the
     *         scrapping started.
     */
    int getSuccessfullTasks();

    /**
     * @return the number of failed tasks since the scrapping started.
     */
    int getFailedTasks();

    /**
     * @return the number of tasks remaining tasks. This includes the sum
     *         of tasks yet not started and those actually running
     *         {@link #getRunningTasks()}.
     */
    int getRemaining();
}
