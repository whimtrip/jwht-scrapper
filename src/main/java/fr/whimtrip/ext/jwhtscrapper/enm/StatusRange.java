package fr.whimtrip.ext.jwhtscrapper.enm;


import fr.whimtrip.ext.jwhtscrapper.annotation.WarningSign;
import fr.whimtrip.ext.jwhtscrapper.service.base.RequestSynchronizer;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     Status Range enum defining the types of HTTP responses obtained.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public enum StatusRange {

    /**
     * Any valid 2xx HTTP request Status code. This mean
     * the request is supposed to have work properly. This
     * being said, for web scrapping, 2xx response doesn't
     * mean that the obtained HTTP document is the one
     * expected. The server might return an HTML page
     * with a "Are you a robot" iframe from Google Captcha
     * technology for example... This kind of problems
     * should be detected with a proper {@link WarningSign}
     * setup.
     */
    WORKED_2XX(2),

    /**
     * Any 3xx HTTP responses. Usually : 304 when the response
     * hasn't been modified since last time (in cache), or most
     * redirections status codes.
     */
    ON_HOLD_3XX(3),

    /**
     * <p>
     *      Might be a 401/403 security error which means you are not
     *      properly logged in, that you have been banned or that
     *      you didn't use the proper means of authentication.
     * </p>
     *
     * <p>
     *      Might also be a 400/422 (bad input form typically, check
     *      your config!).
     * </p>
     *
     * <p>404 for not found web pages.</p>
     *
     * <p>And many other... This commonly defines client side errors.</p>
     */
    FAILED_4XX(4),

    /**
     * Internal Server Error. Often happens on the proxy server
     * and not directly on the web server you are trying to scrap.
     */
    FAILED_5XX(5),

    /**
     * When a request times out
     */
    TIMED_OUT(-2),

    /**
     * When connexion (usually to the proxy) was refused.
     */
    CONNECTION_EXCEPTION(-3),

    /**
     * Any other non standard other HTTP status code.
     */
    OTHER(-1);

    /**
     * The default status code for Unknown uncatched exceptions while
     * performing the requests (this will often be due to network problems
     * but this might also be due to some server exception closing the
     * current request IO).
     */
    public static final int UNKNOWN_EXCEPTION_STATUS_CODE = -100;

    /**
     * The status code to use to log a timeout exception to a {@link RequestSynchronizer}
     */
    public static final int TIMEOUT_STATUS_CODE = -200;

    public static final int CONNECT_EXCEPTION_STATUS_CODE = -300;


    /**
     * The first digit of the status code.
     * {@code 2} for {@code 200 - OK} for example
     */
    private final int statusHundredsDigit;

    /**
     * @param statusHundredsDigit the first digit of the status code.
     *                            {@code 2} for {@code 200 - OK} for example
     */
    StatusRange(final int statusHundredsDigit) {
        this.statusHundredsDigit = statusHundredsDigit;
    }

    /**
     * @param statusCode the status code you want to extract a status range for.
     * @return the corresponding status range.
     */
    public static StatusRange getStatusRange(int statusCode) {

        int statusHundredsDigits = statusCode / 100;
        for(StatusRange sr : StatusRange.values())
            if(sr.statusHundredsDigit == statusHundredsDigits)
                return sr;

        return OTHER;
    }

    /**
     * Getter for {@link #statusHundredsDigit}
     * @return {@link #statusHundredsDigit}.
     */
    public int getStatusHundredsDigit() {
        return statusHundredsDigit;
    }
}