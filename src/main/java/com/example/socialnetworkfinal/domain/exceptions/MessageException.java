package com.example.socialnetworkfinal.domain.exceptions;



/**
 * exception to be thrown when operation on messages don't succeed
 */
public class MessageException extends RuntimeException {

    public MessageException(String message) {
        super(message);
    }
}
