package com.healthcare.keymanagement.exception;

public class KeyNotFoundException extends RuntimeException {

    public KeyNotFoundException(String message) {
        super(message);
    }
}