package com.example.equationsolver.exception;

/** Thrown when the equation text cannot be parsed (bad character, unbalanced brackets, etc). */
public class InvalidEquationException extends RuntimeException {

    public InvalidEquationException(String message) {
        super(message);
    }
}
