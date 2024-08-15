package com.stambul.scanner.jobs.exceptions;

public class RebootException extends RuntimeException {

    public RebootException(String errorMessage) {
        super(errorMessage);
    }

    public RebootException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
