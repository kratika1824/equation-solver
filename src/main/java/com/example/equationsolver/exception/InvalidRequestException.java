package com.example.equationsolver.exception;

/** Thrown when the request itself is wrong (empty body field, bad variable name or value). */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
