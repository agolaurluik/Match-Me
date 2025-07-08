package com.kood.backend.exceptions;

public class DatabaseNotReadyException extends RuntimeException {
    public DatabaseNotReadyException(String message) {
        super(message);
    }
}
