/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper;

/**
 * Created by LOUISSTEIMBERG on 16/11/2017.
 */
public class ScrapperHolder {


    private static final int DEFAULT_SCRAP_LIMIT = 10;

    private int scrapLimit = DEFAULT_SCRAP_LIMIT;

    private int scrapLeft = DEFAULT_SCRAP_LIMIT;

    private int scrappedCorrectly = 0;

    private int scrappedWithError = 0;

    public ScrapperHolder incrementScrappedCorrectly()
    {
        scrappedCorrectly ++;
        scrapLeft --;
        return this;
    }

    public ScrapperHolder incrementScrappedWithError()
    {
        scrappedWithError ++;
        scrapLeft --;
        return this;
    }


    public int getScrapLimit() {
        return scrapLimit;
    }

    public ScrapperHolder setScrapLimit(int scrapLimit) {
        this.scrapLimit = scrapLimit;
        this.scrapLeft = scrapLimit;
        return this;
    }

    public int getScrapLeft() {
        return scrapLeft;
    }

    public ScrapperHolder setScrapLeft(int scrapLeft) {
        this.scrapLeft = scrapLeft;
        return this;
    }

    public int getScrappedCorrectly() {
        return scrappedCorrectly;
    }

    public ScrapperHolder setScrappedCorrectly(int scrappedCorrectly) {
        this.scrappedCorrectly = scrappedCorrectly;
        return this;
    }

    public int getScrappedWithError() {
        return scrappedWithError;
    }

    public ScrapperHolder setScrappedWithError(int scrappedWithError) {
        this.scrappedWithError = scrappedWithError;
        return this;
    }
}
