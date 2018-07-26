package fr.whimtrip.ext.jwhtscrapper.service.scoped;

/**
 * <p>Part of project jwht-scrapper</p>
 * Created on 26/07/18
 * @author Louis-wht
 * @since 1.0.0
 */
public class ScrappingStats {

    private final int finishedTasks;
    private final int runningTasks;
    private final int validFinishedTasks;
    private final int failedFinishedTasks;
    private final int remaining;

    public ScrappingStats(int finishedTasks, int runningTasks, int validFinishedTasks, int failedFinishedTasks, int remaining) {

        this.finishedTasks = finishedTasks;
        this.runningTasks = runningTasks;
        this.validFinishedTasks = validFinishedTasks;
        this.failedFinishedTasks = failedFinishedTasks;
        this.remaining = remaining;
    }

    public int getFinishedTasks() {

        return finishedTasks;
    }

    public int getRunningTasks() {

        return runningTasks;
    }

    public int getValidFinishedTasks() {

        return validFinishedTasks;
    }

    public int getFailedFinishedTasks() {

        return failedFinishedTasks;
    }

    public int getRemaining() {

        return remaining;
    }
}
