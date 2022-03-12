package com.example.socialnetworkfinal.domain.exceptions;


/**
 * Exception to be thrown when operations on users don't succeed
 */
public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }
}
