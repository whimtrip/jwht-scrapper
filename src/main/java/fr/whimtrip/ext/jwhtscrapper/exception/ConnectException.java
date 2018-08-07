/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.exception;

import org.asynchttpclient.Response;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 29/07/18</p>
 *
 * <p>
 *     Thrown when connext before proxy request is set to
 *     true and when the connection (TCP CONNECT) cannot be
 *     made properly.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public class ConnectException extends RequestFailedException {
    public ConnectException(Response resp) {
        super(resp);
    }

    public ConnectException(Throwable e)
    {
        super(e.getMessage());
        super.setStackTrace(e.getStackTrace());
    }

}
