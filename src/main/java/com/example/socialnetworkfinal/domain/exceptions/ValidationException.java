package com.example.socialnetworkfinal.domain.exceptions;


/**
 * Exception class for validation
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

}

