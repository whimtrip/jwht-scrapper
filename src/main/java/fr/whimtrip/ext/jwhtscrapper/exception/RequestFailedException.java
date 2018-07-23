package fr.whimtrip.ext.jwhtscrapper.exception;

import org.asynchttpclient.Response;

public class RequestFailedException extends ScrapperException {

    private static final int MAX_BODY_LENGTH = 1000;

    public RequestFailedException(Response resp)
    {
        super(
                String.format(
                        "Http request failed with status %s - %s  and body %s",
                        resp.getStatusCode(),
                        resp.getStatusText(),
                        buildBody(resp)
                )
        );
    }

    private static String buildBody(Response resp) {

        String body = resp.getResponseBody();
        if(body.length() > MAX_BODY_LENGTH)
            body = body.substring(0, MAX_BODY_LENGTH);
        return body;
    }

    public RequestFailedException(String errorMessage) {
        super(errorMessage);
    }
}
