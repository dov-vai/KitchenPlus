package com.kitchenplus.kitchenplus.exceptions;

public class UserFriendlyException extends Exception {

    public UserFriendlyException(String message) {
        super(message);
    }

    public UserFriendlyException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserFriendlyException(Throwable cause) {
        super(cause);
    }
}
