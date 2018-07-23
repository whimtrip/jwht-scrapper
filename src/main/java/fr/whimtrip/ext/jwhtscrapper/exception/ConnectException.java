/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.exception;

import org.asynchttpclient.Response;

/**
 * Created by LOUISSTEIMBERG on 23/11/2017.
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
