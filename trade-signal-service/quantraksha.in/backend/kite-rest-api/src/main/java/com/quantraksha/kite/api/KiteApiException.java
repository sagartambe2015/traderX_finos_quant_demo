package com.quantraksha.kite.api;

import org.springframework.http.HttpStatus;

/** Unchecked carrier for Kite errors; Kite's own KiteException extends Throwable, which Spring MVC won't pass through. */
public class KiteApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorType;

    public KiteApiException(HttpStatus status, String errorType, String message) {
        super(message);
        this.status = status;
        this.errorType = errorType;
    }

    public HttpStatus status() {
        return status;
    }

    public String errorType() {
        return errorType;
    }
}
