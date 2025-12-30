package com.tilingroofing.common.exception;

/**
 * Exception thrown when an uploaded file is invalid.
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}

