package com.example.socialnetworkfinal.domain.exceptions;

/**
 * Exception to be thrown when operations on friendships don't succeed
 */
public class FriendshipException extends RuntimeException {

    public FriendshipException(String message) {
        super(message);
    }
}
