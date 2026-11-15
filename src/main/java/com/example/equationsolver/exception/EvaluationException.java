package com.example.equationsolver.exception;

/** Thrown when the maths itself fails, e.g. division by zero. */
public class EvaluationException extends RuntimeException {

    public EvaluationException(String message) {
        super(message);
    }
}
